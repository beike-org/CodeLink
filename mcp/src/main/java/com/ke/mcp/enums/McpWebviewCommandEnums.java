package com.ke.mcp.enums;

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
public enum McpWebviewCommandEnums implements WebviewCommand {

    //------------------MCP---------------------//

    MCP_STATUS_SWITCH("mcpStatusSwitch", "wtp"),

    ADD_MCP_MARKET("addMcpMarketServer", "wtp"),



    MCP_DETAIL_LIST("mcpDetailList", "wtp"),

    MCP_WEBVIEW_REFRESH("mcpWebviewRefresh", "ptw");

    private String command;

    private String type;
}
