package com.ke.agentic.socket.handler;

import com.intellij.execution.OutputListener;
import com.intellij.execution.configurations.GeneralCommandLine;
import com.intellij.execution.process.ProcessEvent;
import com.intellij.execution.process.ProcessHandler;
import com.intellij.execution.process.ProcessHandlerFactory;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Key;
import com.ke.agentic.socket.CancelCountDownLatch;
import com.ke.agentic.socket.RequestEntity;
import com.ke.agentic.socket.SideCarSocketCacheManager;
import com.ke.agentic.socket.dto.CommandDTO;
import com.ke.agentic.socket.dto.CommandResultDTO;
import com.ke.utils.BaseLocalBinaryToolUtil;
import com.ke.utils.FileUtil;
import com.ke.utils.JsonUtil;
import fi.iki.elonen.NanoHTTPD;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

/**
 * @Author: zhangshaoxun001
 * @Date: 2025/3/10 16:11
 * @Description
 */
public class CommandHandler implements RequestHandler {

	private static final Logger LOG = Logger.getInstance(CommandHandler.class);

	// 等待用户点击执行的最长时间
	private static final Long WAIT_USER_TIMEOUT = 30000L;

	// 进程的最大执行时间
	private static final Long PROCESS_TIMEOUT = 60000L;

	@Override
	public String getPath() {
		return "/agentic/execute_terminal_command";
	}

	@Override
	public NanoHTTPD.Method getMethod() {
		return NanoHTTPD.Method.POST;
	}

	@Override
	public NanoHTTPD.Response doHandle(Project project, RequestEntity requestEntity) {
		CancelCountDownLatch countDownLatch = new CancelCountDownLatch(1);
		try {
			CommandDTO commandDTO = JsonUtil.getData(requestEntity.getBody(), CommandDTO.class);
			SideCarSocketCacheManager sideCarSocketCacheManager = project.getService(SideCarSocketCacheManager.class);
			boolean autoRun = Boolean.TRUE.equals(commandDTO.getAutoRun());
			if (!autoRun) {
				sideCarSocketCacheManager.cacheCommand(commandDTO.getCommandId(), countDownLatch);
			}

			boolean run = autoRun || countDownLatch.await(WAIT_USER_TIMEOUT, TimeUnit.MILLISECONDS);
			if (run) {
				if (!autoRun && countDownLatch.isCancel()) {
					// 在等待时间内，用户点击了取消
					return parseResponse(NanoHTTPD.Response.Status.OK, CommandResultDTO.builder().state("cancel").output("user cancel").build());
				}

				try {
					GeneralCommandLine commandLine = BaseLocalBinaryToolUtil.getCommandlineWithShell(Arrays.asList(commandDTO.getCommand().split(" ")), null);
					commandLine.setWorkDirectory(FileUtil.getProjectRootPath(project));

					ProcessHandler processHandler = ProcessHandlerFactory.getInstance().createColoredProcessHandler(commandLine);

					StringBuilder stringBuilder = new StringBuilder();
					AtomicReference<String> type = new AtomicReference<>("stdout");
					CancelCountDownLatch processLatch = new CancelCountDownLatch(1);
					sideCarSocketCacheManager.cacheProcess(commandDTO.getCommandId(), processLatch);

					processHandler.addProcessListener(new OutputListener() {

						@Override
						public void onTextAvailable(@NotNull ProcessEvent event, @NotNull Key outputType) {
							stringBuilder.append(event.getText());
							if (outputType.toString().equals("stderr")) {
								type.set("stderr");
							}
						}

						@Override
						public void processTerminated(@NotNull ProcessEvent event) {
							processLatch.countDown();
							sideCarSocketCacheManager.processCompleted(commandDTO.getCommandId());
						}

					});

					processHandler.startNotify();
					if (processLatch.await(PROCESS_TIMEOUT, TimeUnit.MILLISECONDS)) {
						if (processLatch.isCancel()) {
							if (!processHandler.isProcessTerminated() || !processHandler.isProcessTerminating()) {
								processHandler.destroyProcess();
							}
							// 用户点击执行后，又点击取消
							return parseResponse(NanoHTTPD.Response.Status.OK, CommandResultDTO.builder().state("cancel").output("user cancel").build());
						}
						// 进程成功执行完成
						return parseResponse(NanoHTTPD.Response.Status.OK, CommandResultDTO.builder().state("run").output(stringBuilder.toString()).outputType(type.get()).build());

					}

					// 进程执行超时
					return parseResponse(NanoHTTPD.Response.Status.OK, CommandResultDTO.builder().state("cancel").output("process execute timeout").build());

				} catch (Exception e) {
					LOG.error("executeCommand error", e);
					return parseResponse(NanoHTTPD.Response.Status.OK, CommandResultDTO.builder().state("cancel").output(e.getMessage()).build());
				}

			}
			// 在等待时间内，用户没有点击执行或取消
			return parseResponse(NanoHTTPD.Response.Status.OK, CommandResultDTO.builder().state("cancel").output("wait user timeout").build());
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
		}
		// 进程中断
		return parseResponse(NanoHTTPD.Response.Status.OK, CommandResultDTO.builder().state("cancel").output("Thread interrupt").build());
	}
}
