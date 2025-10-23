package com.ke.agentic;

import com.intellij.execution.ExecutionException;
import com.intellij.execution.configurations.GeneralCommandLine;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.util.EmptyRunnable;
import com.intellij.util.ConcurrencyUtil;
import com.ke.utils.PluginUtil;
import com.ke.utils.ProcessUtil;
import org.jetbrains.annotations.NotNull;

import java.time.Duration;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

public class SideCarAgentProcessService {
	private static final Logger LOG = Logger.getInstance(SideCarAgentProcessService.class);

	private final AtomicBoolean isShutdown = new AtomicBoolean(false);
	private final ExecutorService agentExecutor = ConcurrencyUtil.newSingleThreadExecutor("SideCar agent");

	@NotNull
	private final SideCarAgentProcessHandler agentProcess;


	public SideCarAgentProcessService(int agentPort) throws ExecutionException {
		this.agentProcess = launchAgent(agentPort);
	}


	public boolean isRunning() {
		return !this.agentProcess.isProcessTerminated() && !this.agentProcess.isProcessTerminating();
	}


	public void startNotify() {
		this.agentProcess.startNotify();
	}

	public boolean isShutdown() {
		return this.isShutdown.get();
	}

	public void shutdown() {
		if (!this.isShutdown.compareAndSet(false, true)) {
			throw new IllegalStateException("agent was already shutdown");
		} else {
			boolean isAlive = !ApplicationManager.getApplication().isDisposed();

			this.agentExecutor.shutdown();
			ProcessUtil.waitForProcessTermination(this.agentProcess, Duration.ofSeconds(5L), Duration.ofMillis(100L));
			try {
				if (isAlive) {
					this.agentExecutor.awaitTermination(1L, TimeUnit.SECONDS);
				}
			} catch (InterruptedException interruptedException) {
				Thread.currentThread().interrupt();
				LOG.error("error waiting for agent termination", interruptedException);
			} catch (Exception exception) {
				LOG.error("error awaiting agent termination", exception);
			}

		}
	}

	public void flush() {
		if (this.isShutdown()) {
			LOG.debug("flush called for shutdown agent");
		} else {
			try {
				this.agentExecutor.submit(EmptyRunnable.INSTANCE).get(15L, TimeUnit.SECONDS);
			} catch (Exception exception) {
				throw new RuntimeException("Error flushing agent executor service", exception);
			}
		}
	}


	private static @NotNull SideCarAgentProcessHandler launchAgent(Integer agentPort) throws ExecutionException {
		String env = PluginUtil.getEnv();
		GeneralCommandLine cmdline = SideCarAgentUtil.getCommandline(List.of(SideCarAgentUtil.getBinaryPath(), "--port", String.valueOf(agentPort), "--env", env));
		return new SideCarAgentProcessHandler(cmdline);
	}

}
