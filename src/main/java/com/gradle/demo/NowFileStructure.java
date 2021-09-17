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

import javax.swing.*;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;

public class NowFileStructure {
    DefaultMutableTreeNode root = new DefaultMutableTreeNode("Interest Code Elements");
    ArrayList allPaths = new ArrayList<String>();
    ArrayList newpath = new ArrayList<String>();
    private Project project;
    private ToolWindow toolWindow;
    private ToolWindow showToolWindow;
    private VirtualFile myFile;
    private StructureView structureView;
    private boolean firstRefresh = true;
    private int count;
    private int nowId;
    public NowFileStructure(Project project,ToolWindow toolWindow) {
        ToolWindowManager.getInstance(project).registerToolWindow(new RegisterToolWindowTask("InterestCodeElement", ToolWindowAnchor.RIGHT,null,true,false,true,true,null, AllIcons.Toolwindows.ToolWindowStructure,null));
        ToolWindowManager windowManager = ToolWindowManager.getInstance(project);
        showToolWindow = windowManager.getToolWindow("InterestCodeElement");
        showToolWindow.show();
        this.project = project;
        this.toolWindow = toolWindow;
        Timer timer = TimerUtil.createNamedTimer("nowFileStructure", 100, event -> {
            int count = ActivityTracker.getInstance().getCount();
            if (count == this.count) {
                return;
            }
            refresh();
            this.count = count;
        });
        timer.start();
    }
    private void refresh() {
        Component owner = KeyboardFocusManager.getCurrentKeyboardFocusManager().getFocusOwner();
        boolean ifContact = SwingUtilities.isDescendingFrom(toolWindow.getComponent(), owner);
        DataContext dataContext = DataManager.getInstance().getDataContext(owner);
        if (CommonDataKeys.PROJECT.getData(dataContext) != project || (!firstRefresh && (ifContact || JBPopupFactory.getInstance().isPopupActive()))){
            return;
        }
        VirtualFile[] files = ifContact ? null : CommonDataKeys.VIRTUAL_FILE_ARRAY.getData(dataContext);
        if (files != null && files.length == 1) {
            newFile(files[0]);
        }
        else if (firstRefresh) {
            FileEditorManagerImpl editorManager = (FileEditorManagerImpl) FileEditorManager.getInstance(project);
            java.util.List<Pair<VirtualFile, EditorWindow>> history = editorManager.getSelectionHistory();
            if (!history.isEmpty()) {
                newFile(history.get(0).getFirst());
            }
        }
        firstRefresh = false;
    }
    private void newFile(VirtualFile file) {
        if (!Comparing.equal(file, myFile)) {
            myFile = file;
            rebuild();
        }
    }

    public void rebuild() {
        ContentManager contentManager = toolWindow.getContentManager();
        contentManager.removeAllContents(true);
        VirtualFile file = myFile;
        if (file == null) {
            VirtualFile[] selectedFiles = FileEditorManager.getInstance(project).getSelectedFiles();
            if (selectedFiles.length > 0) {
                file = selectedFiles[0];
            }
        }

        String[] names = {""};
        FileEditor editor = FileEditorManager.getInstance(project).getSelectedEditor(file);
        if (file.isDirectory() || editor == null || !editor.isValid()) {
            return;
        }
        if (file != null && file.isValid()) {
            StructureViewBuilder structureViewBuilder = editor.getStructureViewBuilder();
            if (structureViewBuilder != null) {
                structureView = structureViewBuilder.createStructureView(editor, project);
                treeListener();
                createPanel(structureView.getComponent());
            }
        }
    }

    private void treeListener() {
        JTree tree = ((StructureViewComponent) structureView).getTree();
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
    }

private void createPanel(Component component){
    MyPanel myPanel = new MyPanel();
    myPanel.setBackground(UIUtil.getTreeBackground());
    myPanel.add(component, BorderLayout.CENTER);
    Content content = ContentFactory.SERVICE.getInstance().createContent(myPanel, "", false);
    ContentManager contentManager = toolWindow.getContentManager();
    contentManager.removeAllContents(true);
    contentManager.addContent(content);
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
