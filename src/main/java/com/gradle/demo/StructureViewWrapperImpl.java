package com.gradle.demo;

import com.gradle.demo.windows.InterestCodeElementsImpl;
import com.intellij.icons.AllIcons;
import com.intellij.ide.ActivityTracker;
import com.intellij.ide.DataManager;
import com.intellij.ide.structureView.StructureView;
import com.intellij.ide.structureView.StructureViewBuilder;
import com.intellij.ide.structureView.newStructureView.StructureViewComponent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.actionSystem.DataContext;
import com.intellij.openapi.fileEditor.FileEditor;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.fileEditor.impl.EditorWindow;
import com.intellij.openapi.fileEditor.impl.FileEditorManagerImpl;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.popup.JBPopupFactory;
import com.intellij.openapi.util.Comparing;
import com.intellij.openapi.util.Pair;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.wm.RegisterToolWindowTask;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ToolWindowAnchor;
import com.intellij.openapi.wm.ToolWindowManager;
import com.intellij.ui.content.Content;
import com.intellij.ui.content.ContentFactory;
import com.intellij.ui.content.ContentManager;
import com.intellij.util.ui.TimerUtil;
import com.intellij.util.ui.UIUtil;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;

public final class StructureViewWrapperImpl{
    DefaultMutableTreeNode root = new DefaultMutableTreeNode("Interest Code Elements");
    ArrayList allPaths = new ArrayList<String>();
    ArrayList newpath = new ArrayList<String>();
    private static final int REFRESH_TIME = 100; // time to check if a context file selection is changed or not
    private final Project myProject;
    private final ToolWindow myToolWindow;
    private ToolWindow showToolWindow;
    private VirtualFile myFile;
    private StructureView myStructureView;
    private JPanel[] myPanels = new JPanel[0];
    private boolean myFirstRun = true;
    private int myActivityCount;
    private int nowId;
    public StructureViewWrapperImpl(@NotNull Project project, @NotNull ToolWindow toolWindow) {
        ToolWindowManager.getInstance(project).registerToolWindow(new RegisterToolWindowTask("InterestCodeElement", ToolWindowAnchor.RIGHT,null,true,false,true,true,null, AllIcons.Toolwindows.ToolWindowStructure,null));
        ToolWindowManager windowManager = ToolWindowManager.getInstance(project);
        showToolWindow = windowManager.getToolWindow("InterestCodeElement");
        showToolWindow.show();
        myProject = project;
        myToolWindow = toolWindow;
        Timer timer = TimerUtil.createNamedTimer("StructureView", REFRESH_TIME, event -> {
            int count = ActivityTracker.getInstance().getCount();
            if (count == myActivityCount) {
                return;
            }
            checkUpdate();
            myActivityCount = count;
        });
        timer.start();
    }
    private void checkUpdate() {
        final Component owner = KeyboardFocusManager.getCurrentKeyboardFocusManager().getFocusOwner();
        final boolean insideToolwindow = SwingUtilities.isDescendingFrom(myToolWindow.getComponent(), owner);
        if (!myFirstRun) {
            if(insideToolwindow){return;}
            if(JBPopupFactory.getInstance().isPopupActive()){return;}
        }
        final DataContext dataContext = DataManager.getInstance().getDataContext(owner);
        if (CommonDataKeys.PROJECT.getData(dataContext) != myProject) {
            return;
        }
        VirtualFile[] files = insideToolwindow ? null : CommonDataKeys.VIRTUAL_FILE_ARRAY.getData(dataContext);

        if (files != null && files.length == 1) {
            setFile(files[0]);
        }
        else if (myFirstRun) {
            FileEditorManagerImpl editorManager = (FileEditorManagerImpl)FileEditorManager.getInstance(myProject);
            List<Pair<VirtualFile, EditorWindow>> history = editorManager.getSelectionHistory();
            if (!history.isEmpty()) {
                setFile(history.get(0).getFirst());
            }
        }
        myFirstRun = false;
    }
    private void setFile(VirtualFile file) {
        if (!Comparing.equal(file, myFile)) {
            myFile = file;
            rebuild();
        }
    }

    public void rebuild() {
        final ContentManager contentManager = myToolWindow.getContentManager();
        contentManager.removeAllContents(true);
        VirtualFile file = myFile;
        if (file == null) {
            final VirtualFile[] selectedFiles = FileEditorManager.getInstance(myProject).getSelectedFiles();
            if (selectedFiles.length > 0) {
                file = selectedFiles[0];
            }
        }
        String[] names = {""};
        if (file != null && file.isValid()) {
            if (file.isDirectory()) {
            }
            else {
                FileEditor editor = FileEditorManager.getInstance(myProject).getSelectedEditor(file);
                StructureViewBuilder structureViewBuilder = editor.getStructureViewBuilder();
                if (structureViewBuilder != null) {
                    myStructureView = structureViewBuilder.createStructureView(editor, myProject);
                    JTree tree = ((StructureViewComponent)myStructureView).getTree();
                    tree.addMouseListener(new MouseAdapter() {
                        @Override
                        public void mouseClicked(MouseEvent e) {
                            super.mouseClicked(e);
                            TreePath selectionPath = tree.getSelectionPath();
                            newpath.clear();
                            if(!allPaths.contains(selectionPath.toString())){

                                allPaths.add(selectionPath.toString());
                                Object[] path = selectionPath.getPath();

                                for(int i=0;i<path.length;i++){
                                    newpath.add(path[i]);
                                }
                                buildTree();
                            }
                        }
                    });
                    createSinglePanel(myStructureView.getComponent());
                }
            }
        }
        for (int i = 0; i < myPanels.length; i++) {
            final Content content = ContentFactory.SERVICE.getInstance().createContent(myPanels[i], names[i], false);
            contentManager.addContent(content);
        }
    }

    private void createSinglePanel(final JComponent component) {
        myPanels = new JPanel[1];
        myPanels[0] = createPanel(component);
    }
    private MyPanel createPanel(JComponent component) {
        final MyPanel panel = new MyPanel();
        panel.setBackground(UIUtil.getTreeBackground());
        panel.add(component, BorderLayout.CENTER);
        return panel;
    }
    private class MyPanel extends JPanel{
        MyPanel() {
            super(new BorderLayout());
        }
    }

    private void buildTree(){
        DefaultMutableTreeNode insertNode = selectInsertNode();
        for(;nowId<newpath.size();nowId++){
            DefaultMutableTreeNode newNode = new DefaultMutableTreeNode(newpath.get(nowId).toString());
            insertNode.add(newNode);
            insertNode = newNode;
        };
        ContentManager myContentManager = showToolWindow.getContentManager();
        myContentManager.removeAllContents(true);
        InterestCodeElementsImpl myInterest = new InterestCodeElementsImpl(root,allPaths);
        ContentFactory contentFactory = ContentFactory.SERVICE.getInstance();
        Content content = contentFactory.createContent(myInterest.getContent(), "", false);
        showToolWindow.getContentManager().addContent(content);
    }
    private DefaultMutableTreeNode selectInsertNode(){
        DefaultMutableTreeNode nowNode = root;
        DefaultMutableTreeNode insertNode = root;
        boolean t = true;
        for( nowId = 0; nowId < newpath.size(); nowId++){
            if(!t){break;}
            Object now = newpath.get(nowId);
            if(nowNode.getChildCount() == 0){
                insertNode = nowNode;
                break;
            }
            TreeNode nowChild = nowNode.getFirstChild();
            while(nowNode.getChildAfter(nowChild) != null && nowChild.toString() != now.toString()){
                nowChild = nowNode.getChildAfter(nowChild);
            }
            if(nowChild.toString() != now.toString()){
                t = false;
                insertNode = (DefaultMutableTreeNode) nowChild.getParent();
                break;
            }
            else{
                nowNode=(DefaultMutableTreeNode) nowChild;
            }
        }
        return insertNode;
    }
}
