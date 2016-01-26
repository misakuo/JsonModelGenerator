package com.moxun.generator;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.project.Project;

/**
 * 插件入口
 * Created by moxun on 15/11/26.
 */
public class Generator extends AnAction{
    @Override
    public void actionPerformed(AnActionEvent anActionEvent) {
        Project project = anActionEvent.getProject();
        Logger.init();
        GeneratorForm form = new GeneratorForm(project);
        form.show();
    }
}
