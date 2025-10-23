package com.ke.webview.communication.handler.wtp;

import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
import com.ke.service.notify.NotifyServiceImpl;
import com.ke.utils.JsonUtil;
import com.ke.webview.BaseCommandEnums;
import com.ke.webview.dto.AlertDTO;
import org.apache.commons.lang.StringUtils;

import java.util.Objects;

/**
 * @Author: zhangshaoxun001
 * @Date: 2023/8/16 16:53
 * @Version 1.0
 * @Description
 */
public class AlertWTPHandler extends BaseWTPHandler {

    private final static Logger LOG = Logger.getInstance(AlertWTPHandler.class);

    public AlertWTPHandler(Project project) {
        super(s -> {
            AlertDTO alertDTO = JsonUtil.getData(s, AlertDTO.class);
            String content = alertDTO.getContent();
            if (Objects.isNull(content)) {
                LOG.warn("webview alert content is null:" + alertDTO);
                return null;
            }
            NotifyServiceImpl notify = project.getService(NotifyServiceImpl.class);
            String title = StringUtils.defaultIfEmpty(alertDTO.getTitle(), "webview message");
            switch (alertDTO.getType()) {
                case "warn": {
                    notify.warn(title, content);
                    break;
                }
                case "error": {
                    notify.error(title, content);
                    break;
                }
                default: {
                    notify.info(title, content);
                }
            }
            return null;
        });
    }

    @Override
    public String getCommand() {
        return BaseCommandEnums.ALERT.getCommand();
    }
}
