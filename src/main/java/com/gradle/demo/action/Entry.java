package com.gradle.demo.action;

import com.intellij.icons.AllIcons;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.PlatformDataKeys;
import com.intellij.openapi.project.DumbAwareAction;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.wm.*;

public class Entry extends DumbAwareAction {
    @Override
    public void actionPerformed(AnActionEvent e) {
        Project project = e.getData(PlatformDataKeys.PROJECT);
        ToolWindowManager windowManager = ToolWindowManager.getInstance(project);
        ToolWindow toolWindow = windowManager.getToolWindow(ToolWindowId.STRUCTURE_VIEW);
        toolWindow.hide();
        ToolWindowManager.getInstance(project).registerToolWindow(new RegisterToolWindowTask("testwindow", ToolWindowAnchor.LEFT,null,true,false,true,true,null, AllIcons.Toolwindows.ToolWindowStructure,null));
        ToolWindow testToolWindow = windowManager.getToolWindow("testwindow");
        new NowFileStructure(project, testToolWindow);
        testToolWindow.show();
    }
}

