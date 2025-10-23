package com.ke.rules.webview;

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
public enum RulesWebviewCommandEnums implements WebviewCommand {

    //------------rules---------------//
    RULE_INFO_REQ("ruleInfoReq", "wtp"),

    MANUAL_RULE_LIST_REQ("manualRuleListReq", "wtp"),

    ADD_PROJECT_RULES("addProjectRules", "wtp"),


    /**
     * 用户搜索项目文件
     */
    SEARCH_FILE_LIST("searchFileList", "wtp"),

    /**
     * 用户文件默认展示，逻辑是传输用户打开文件列表
     */
    GET_DEFAULT_FILE_LIST("getDefaultFileList", "ptw"),

    /**
     * 检索文件响应
     */
    SEARCH_FILE_LIST_RESP("searchFileListResp", "ptw"),
    ;

    private final String command;

    private final String type;
}
