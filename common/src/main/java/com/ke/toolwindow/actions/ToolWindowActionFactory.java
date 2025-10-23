package com.ke.toolwindow.actions;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.extensions.ExtensionPointName;

import java.util.List;

/**
 * @Author: zhangshaoxun001
 * @Date: 2024/1/2 16:16
 * @Version 1.0
 * @Description 注册Action到CodeLink ToolWindow上方导航栏
 */
public interface ToolWindowActionFactory {

    ExtensionPointName<ToolWindowActionFactory> EP_NAME = ExtensionPointName.create("com.ke.codelink.toolWindowActionFactory");


    List<AnAction> getActions();

}
