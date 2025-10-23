package com.ke.agentic.socket.handler;

import com.alibaba.fastjson.JSONObject;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.project.Project;
import com.ke.exception.ExceptionEnum;
import com.ke.service.notify.NotifyServiceImpl;
import com.ke.agentic.socket.RequestEntity;
import com.ke.agentic.socket.SideCarSocketCacheManager;
import com.ke.agentic.socket.dto.ApplyFileDTO;
import com.ke.utils.FileUtil;
import com.ke.utils.JsonUtil;
import fi.iki.elonen.NanoHTTPD;

import java.util.Objects;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

/**
 * @Author: zhangshaoxun001
 * @Date: 2025/3/10 16:11
 * @Description
 */
public class ApplyFileHandler implements RequestHandler {

	private static final Logger LOG = Logger.getInstance(ApplyFileHandler.class);

	@Override
	public String getPath() {
		return "/agentic/apply_stream_edit";
	}

	@Override
	public NanoHTTPD.Method getMethod() {
		return NanoHTTPD.Method.POST;
	}

	@Override
	public NanoHTTPD.Response doHandle(Project project, RequestEntity requestEntity) {
		ApplyFileDTO applyFileDTO = JsonUtil.getData(requestEntity.getBody(), ApplyFileDTO.class);
		LOG.info("ApplyFileHandler: " + requestEntity.getBody());
		String filePath = applyFileDTO.getFilePath();
		String event = applyFileDTO.getEvent();
		JSONObject res = new JSONObject();
		SideCarSocketCacheManager sideCarSocketCacheManager = project.getService(SideCarSocketCacheManager.class);
		// start事件，如果文件不存在就创建文件
		if ("start".equalsIgnoreCase(event)) {
			if (!FileUtil.isFileExist(filePath)) {
				CountDownLatch countDownLatch = new CountDownLatch(1);
				LOG.info("ApplyFileHandler, start event, file not exists, create file: " + filePath);
				sideCarSocketCacheManager.cacheNewFileCountDownLatch(filePath, countDownLatch);
				ApplicationManager.getApplication().invokeLater(() -> WriteCommandAction.runWriteCommandAction(project, () -> {
					try {
						FileUtil.createProjectFile(filePath, project, null);
						res.put("status", "success");
					} catch (Exception e) {
						project.getService(NotifyServiceImpl.class).notifyException(e);
						res.put("status", "error");
						res.put("message", e.getMessage());
					} finally {
						sideCarSocketCacheManager.removeNewFileCountDownLatch(filePath);
						countDownLatch.countDown();
					}
				}));
				return waitCountDownLatch(countDownLatch, res);
			}
			res.put("status", "success start");
			return parseResponse(NanoHTTPD.Response.Status.OK, res);
		} else {
			// 其他事件，等待文件先创建完成
			CountDownLatch newFileCountDownLatch = sideCarSocketCacheManager.getNewFileCountDownLatch(filePath);
			if (Objects.nonNull(newFileCountDownLatch)) {
				try {
					if (!newFileCountDownLatch.await(30000L, TimeUnit.MILLISECONDS)) {
						LOG.warn(event + " event, file not exists, but create file timeout: " + filePath);
						res.put("status", "error");
						res.put("message", "file not exists, but create file timeout");
						return parseResponse(NanoHTTPD.Response.Status.INTERNAL_ERROR, res);
					}
				} catch (InterruptedException interruptedException) {
					Thread.currentThread().interrupt();
					LOG.warn(event + " event, file not exists, but create file error: " + filePath);
					res.put("status", "error");
					res.put("message", interruptedException.getMessage());
					return parseResponse(NanoHTTPD.Response.Status.INTERNAL_ERROR, res);
				} catch (Exception e) {
					LOG.warn(event + " event, file not exists, but create file error: " + filePath);
					res.put("status", "error");
					res.put("message", e.getMessage());
					return parseResponse(NanoHTTPD.Response.Status.INTERNAL_ERROR, res);
				}
			}

		}
		// 其他事件，文件必须存在
		if (!FileUtil.isFileExist(filePath)) {
			ExceptionEnum.FILE_NOT_EXISTS_EXCEPTION.asBusinessException(filePath);
		}

		LOG.info("ApplyFileHandler, edit content request: " + applyFileDTO);

		// 获取code片段
		String code;
		try {
			JSONObject object = JSONObject.parseObject(event);
			code = object.getString("Delta");
		} catch (Exception e) {
			res.put("status", "ignore event");
			return parseResponse(NanoHTTPD.Response.Status.OK, res);
		}


		String finalCode = code;
		CountDownLatch countDownLatch = new CountDownLatch(1);
		ApplicationManager.getApplication().invokeLater(() -> {
			FileUtil.openFile(project, filePath);
			WriteCommandAction.runWriteCommandAction(project, () -> {
				try {
					Document document = FileDocumentManager.getInstance().getDocument(FileUtil.findVirtualFile(filePath));
					assert document != null;
					// 获取文件原先内容
					String oldFileContent = document.getText();
					Integer startLine = applyFileDTO.getRange().getStartPosition().getLine();
					int lineStartOffset = document.getLineStartOffset(startLine);
					Integer endLine = applyFileDTO.getRange().getEndPosition().getLine();
					int lineEndOffset = document.getLineEndOffset(endLine);
					int start = Math.max(0, lineStartOffset);
					int end = Math.min(lineEndOffset, document.getTextLength());
					document.replaceString(start, end, finalCode);
					Editor selectedTextEditor = FileEditorManager.getInstance(project).getSelectedTextEditor();
					if (Objects.nonNull(selectedTextEditor) && selectedTextEditor.getDocument().equals(document)) {
						selectedTextEditor.getCaretModel().moveToOffset(applyFileDTO.getRange().getStartPosition().getByteOffset() + finalCode.length());
					}
					res.put("status", "success");
					LOG.info("ApplyFileHandler, edit content success, filename is:" + filePath);
				} catch (Exception e) {
					res.put("status", "error");
					res.put("message", e.getMessage());
					LOG.info("ApplyFileHandler, edit content error, filename is:" + filePath);
				} finally {
					countDownLatch.countDown();
				}
			});
		});
		return waitCountDownLatch(countDownLatch, res);

	}

	private NanoHTTPD.Response waitCountDownLatch(CountDownLatch countDownLatch, JSONObject res) {
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
