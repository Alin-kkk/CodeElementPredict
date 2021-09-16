package com.gradle.demo.windows;


import javax.swing.*;
import javax.swing.tree.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Enumeration;
public class InterestCodeElementsImpl{
    private JPanel mainpanel;
    private JButton submitButton;
    private JTree tree;
    private JButton clearButton;
    public InterestCodeElementsImpl(DefaultMutableTreeNode root,ArrayList<TreePath> allpath) {
        DefaultTreeModel dt = new DefaultTreeModel(root);
        tree.setModel(dt);
        expandAll(tree, new TreePath(root), true);
        DefaultTreeCellRenderer cellRenderer = (DefaultTreeCellRenderer)tree.getCellRenderer();
        cellRenderer.setLeafIcon(new ImageIcon(""));
        cellRenderer.setOpenIcon(new ImageIcon(""));
        cellRenderer.setClosedIcon(new ImageIcon(""));

        submitButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                showCodeInTerminal();
            }
        });
        clearButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                root.removeAllChildren();
                allpath.clear();
                tree.setModel(null);
            }
        });
    }
    public JPanel getContent() {
        return mainpanel;
    }
    private void showCodeInTerminal() {
    }

    private static void expandAll(JTree tree, TreePath parent, boolean expand)
    {
        TreeNode node = (TreeNode) parent.getLastPathComponent();
        if (node.getChildCount() >= 0)
        {
            for (Enumeration e = node.children(); e.hasMoreElements();)
            {
                TreeNode n = (TreeNode) e.nextElement();
                TreePath path = parent.pathByAddingChild(n);
                expandAll(tree, path, expand);
            }
        }
        if (expand)
        {
            tree.expandPath(parent);
        } else
        {
            tree.collapsePath(parent);
        }
    }
}
