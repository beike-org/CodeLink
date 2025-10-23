package com.ke.agentic.socket.handler;

import com.alibaba.fastjson.JSONObject;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.ke.agentic.socket.RequestEntity;
import com.ke.agentic.socket.dto.FileObjectDTO;
import com.ke.agentic.socket.dto.FileOperationType;
import com.ke.agentic.socket.dto.VersionManageDTO;
import com.ke.utils.ComponentUtil;
import com.ke.utils.FileUtil;
import com.ke.utils.JsonUtil;
import fi.iki.elonen.NanoHTTPD;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

/**
 * @Author: zhangshaoxun001
 * @Date: 2025/3/10 16:11
 * @Description 版本管理处理器
 */
public class VersionManageHandler implements RequestHandler {

    private static final Logger LOG = Logger.getInstance(VersionManageHandler.class);

    @Override
    public String getPath() {
        return "/agentic/version_manage";
    }

    @Override
    public NanoHTTPD.Method getMethod() {
        return NanoHTTPD.Method.POST;
    }

    @Override
    public NanoHTTPD.Response doHandle(Project project, RequestEntity requestEntity) {
        VersionManageDTO versionManageDTO = JsonUtil.getData(requestEntity.getBody(), VersionManageDTO.class);
        LOG.info("VersionManageHandler: " + requestEntity.getBody());

        JSONObject res = new JSONObject();

        // 处理版本管理的逻辑
        CountDownLatch countDownLatch = new CountDownLatch(1);
        ApplicationManager.getApplication().invokeLater(() -> WriteCommandAction.runWriteCommandAction(project, () -> {
            try {
                // 遍历versionManageDTO中的fileList，根据fileOperation执行不同的操作
                if (versionManageDTO.getFileList() != null && !versionManageDTO.getFileList().isEmpty()) {
                    for (FileObjectDTO fileObjectDTO : versionManageDTO.getFileList()) {
                        String filePath = fileObjectDTO.getFilePath();
                        FileOperationType fileOperation = fileObjectDTO.getFileOperation();

                        if (fileOperation == FileOperationType.DELETE) {
                            // 删除操作：关闭并删除文件
                            if (FileUtil.isFileExist(filePath)) {
                                VirtualFile virtualFile = LocalFileSystem.getInstance().findFileByPath(filePath);
                                if (virtualFile != null) {
                                    // 先关闭文件
                                    FileEditorManager.getInstance(project).closeFile(virtualFile);
                                    // 然后删除文件
                                    try {
                                        Files.delete(Paths.get(filePath));
                                        LOG.info("文件已删除: " + filePath);
                                    } catch (Exception e) {
                                        LOG.error("删除文件失败: " + filePath, e);
                                        throw new RuntimeException("删除文件失败: " + filePath, e);
                                    }
                                }
                            } else {
                                LOG.warn("要删除的文件不存在: " + filePath);
                            }
                        } else if (fileOperation == FileOperationType.MODIFY) {
                            // 修改操作：打开并替换文件内容
                            String fileContent = fileObjectDTO.getFileContent();
                            if (FileUtil.isFileExist(filePath)) {
                                // 文件存在，打开并替换内容
                                VirtualFile virtualFile = FileUtil.findVirtualFile(filePath);
                                Document document = FileDocumentManager.getInstance().getDocument(virtualFile);
                                if (document != null) {
                                    // 替换文件内容
                                    document.setText(fileContent);
                                    // 保存文件
                                    FileDocumentManager.getInstance().saveDocument(document);
                                    // 打开文件
                                    FileUtil.openFile(project, filePath);
                                    ComponentUtil.getCodeLinkToolWindow(project).hide();
                                    ComponentUtil.getCodeLinkToolWindow(project).show();
                                    LOG.info("文件内容已替换: " + filePath);
                                }
                            } else {
                                // 文件不存在，创建新文件
                                FileUtil.createProjectFile(filePath, project, fileContent);
                                FileUtil.openFile(project, filePath);
                                LOG.info("文件不存在，已创建并打开新文件: " + filePath);
                            }
                        }
                    }
                }

                res.put("status", "success");
                LOG.info("VersionManageHandler, version manage success");
            } catch (Exception e) {
                res.put("status", "error");
                res.put("message", e.getMessage());
                LOG.error("VersionManageHandler, version manage error", e);
            } finally {
                countDownLatch.countDown();
            }
        }));

        try {
            if (countDownLatch.await(5000L, TimeUnit.MILLISECONDS)) {
                if ("success".equals(res.getString("status"))) {
                    return parseResponse(NanoHTTPD.Response.Status.OK, res);
                }
                return parseResponse(NanoHTTPD.Response.Status.INTERNAL_ERROR, res);
            }
            res.put("status", "error");
            res.put("message", "timeout");
            return parseResponse(NanoHTTPD.Response.Status.INTERNAL_ERROR, res);
        } catch (InterruptedException interruptedException) {
            Thread.currentThread().interrupt();
            res.put("status", "error");
            res.put("message", interruptedException.getMessage());
            return parseResponse(NanoHTTPD.Response.Status.INTERNAL_ERROR, res);
        } catch (Exception e) {
            res.put("status", "error");
            res.put("message", e.getMessage());
            return parseResponse(NanoHTTPD.Response.Status.INTERNAL_ERROR, res);
        }
    }
}