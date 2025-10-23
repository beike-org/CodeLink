package com.ke.agentic.webview.handler.wtp;

import cn.hutool.core.collection.CollectionUtil;
import com.google.common.collect.Maps;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.ke.webview.communication.handler.wtp.BaseWTPHandler;
import com.ke.agentic.webview.handler.dto.AgenticRejectChangeDTO;
import com.ke.utils.FileUtil;
import com.ke.utils.JsonUtil;
import com.ke.agentic.webview.handler.AgenticWebviewCommandEnums;
import org.apache.commons.lang3.StringUtils;

import java.util.Map;

public class AgenticRejectChangeWTPHandler extends BaseWTPHandler {

    private static final Logger log = Logger.getInstance(AgenticRejectChangeWTPHandler.class);

    public AgenticRejectChangeWTPHandler(Project project) {
        super(s -> {

            AgenticRejectChangeDTO agenticRejectChange = JsonUtil.getData(s, AgenticRejectChangeDTO.class);

            if (agenticRejectChange == null) {
                return null;
            }

            if (CollectionUtil.isEmpty(agenticRejectChange.getFileName()) || CollectionUtil.isEmpty(agenticRejectChange.getOldContent()) || agenticRejectChange.getFileName().size() != agenticRejectChange.getOldContent().size()) {
                log.warn("AgenticRejectChangeWTPHandler Data Error, Data is: " + agenticRejectChange);
                return null;
            }

            Map<String, String> fileRevertMap = Maps.newHashMap();

            for (int index = 0; index < agenticRejectChange.getFileName().size(); index++) {
                fileRevertMap.put(agenticRejectChange.getFileName().get(index), agenticRejectChange.getOldContent().get(index));
            }
            ApplicationManager.getApplication().invokeLater(() -> WriteCommandAction.runWriteCommandAction(project, () -> {
                for (Map.Entry<String, String> entry : fileRevertMap.entrySet()) {
                    String filePath = entry.getKey();
                    String finalCode = entry.getValue();
                    try {
                        VirtualFile virtualFile = FileUtil.findVirtualFile(filePath);
                        if (StringUtils.isEmpty(finalCode)) {
                            virtualFile.delete(null);
                            continue;
                        }
                        Document document = FileDocumentManager.getInstance().getDocument(virtualFile);
                        assert document != null;
                        int lineStartOffset = document.getLineStartOffset(0);
                        int start = Math.max(0, lineStartOffset);
                        int end = document.getTextLength();
                        document.replaceString(start, end, finalCode);
                    } catch (Exception e) {
                        log.error("AgenticRejectChangeWTPHandler revert code error, filepath:{}", filePath);
                    }
                }
            }));
            return null;
        });
    }

    @Override
    public String getCommand() {
        return AgenticWebviewCommandEnums.AGENTIC_REJECT_CHANGE.getCommand();
    }
}

