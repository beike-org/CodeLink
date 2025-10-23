package com.ke.agentic;

import com.intellij.execution.ExecutionException;
import com.intellij.execution.configurations.GeneralCommandLine;
import com.intellij.execution.process.KillableProcessHandler;
import com.intellij.execution.process.ProcessAdapter;
import com.intellij.execution.process.ProcessEvent;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.util.Key;
import com.intellij.util.io.BaseDataReader.SleepingPolicy;
import com.intellij.util.io.BaseOutputReader;
import com.ke.agentic.topic.SideCarNotifier;
import org.jetbrains.annotations.NotNull;

public class SideCarAgentProcessHandler extends KillableProcessHandler {
	private static final Logger LOG = Logger.getInstance(SideCarAgentProcessHandler.class);

	public SideCarAgentProcessHandler(@NotNull GeneralCommandLine cmdline) throws ExecutionException {
		super(cmdline);
		// 这里messageHandler是共享对象
		this.addProcessListener(new ProcessAdapter() {
			/**
			 * 当进程有输出时，调用这里
			 */
			public void onTextAvailable(@NotNull ProcessEvent event, @NotNull Key outputType) {
				String output = event.getText();
				System.out.println("source is:" + event.getSource().toString() + "agent out is:" + output);
			}

			@Override
			public void startNotified(@NotNull ProcessEvent event) {
				System.out.println("Sidecar Agent process started.");
				SideCarAgentManager.getInstance().setAgentStarted(true);
				SideCarAgentManager.getInstance().healthCheck();
			}

			@Override
			public void processTerminated(@NotNull ProcessEvent event) {
				int exitCode = event.getExitCode();
				SideCarAgentManager.getInstance().setAgentStarted(false);
				SideCarAgentManager.getInstance().setAgentReady(false);
				if (exitCode != 0) {
					LOG.error("Sidecar Agent process terminated with exit code: " + exitCode);
				}
			}
		});
	}


	@NotNull
	protected BaseOutputReader.Options readerOptions() {
		return new AgentOutputReaderOptions();
	}


	private static class AgentOutputReaderOptions extends BaseOutputReader.Options {
		private AgentOutputReaderOptions() {
		}

		public SleepingPolicy policy() {
			return SleepingPolicy.BLOCKING;
		}

		public boolean splitToLines() {
			return false;
		}
	}
}

