package com.ke.agentic.webview.handler;

import com.ke.webview.WebviewCommand;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * @Author: zhangshaoxun001
 * @Date: 2025/6/26 17:02
 * @Description
 */
@Getter
@AllArgsConstructor
public enum AgenticWebviewCommandEnums implements WebviewCommand {

    //---------------------agentic-------------------------//

    /**
     * 用户选中代码
     */
    ADD_TO_CHAT("addToChat", "ptw"),

    /**
     * 执行命令
     */
    EXECUTE_COMMAND("executeCommand", "wtp"),

    /**
     * 取消命令执行
     */
    CANCEL_COMMAND("cancelCommand", "wtp"),


    /**
     * show diff
     */
    AGENTIC_DIFF_FILE("agenticDiffFile", "wtp"),


    /**
     * 拒绝本次修改
     */
    AGENTIC_REJECT_CHANGE("reject", "wtp"),




    /**
     * 用户搜索项目文件夹
     */
    SEARCH_DIRECTORY("searchDirectory", "wtp"),


    /**
     * 获取项目目录树结构
     */
    DEFAULT_DIRECTORY_LIST("defaultDirectoryList", "wtp");

    private String command;

    private String type;
}
