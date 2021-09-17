package com.gradle.demo.action;

import com.gradle.demo.NowFileStructure;
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

        ToolWindowManager.getInstance(project).registerToolWindow(new RegisterToolWindowTask("nowFileWindow", ToolWindowAnchor.LEFT,null,true,false,true,true,null, AllIcons.Toolwindows.ToolWindowStructure,null));
        ToolWindow nowFileView = windowManager.getToolWindow("nowFileWindow");

        new NowFileStructure(project, nowFileView);
        nowFileView.show();
    }
}

