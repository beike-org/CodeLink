package com.ke.agentic.socket;

import com.google.common.collect.Maps;
import com.intellij.openapi.components.Service;
import com.intellij.openapi.project.Project;

import java.util.Map;
import java.util.Objects;
import java.util.concurrent.CountDownLatch;


/**
 * 缓存SideCar发送过来的命令
 */
@Service(Service.Level.PROJECT)
public class SideCarSocketCacheManager {

	private final Project project;

	private final Map<String, CancelCountDownLatch> commandCache = Maps.newConcurrentMap();

	private final Map<String, CancelCountDownLatch> processCache = Maps.newConcurrentMap();

	private final Map<String, CountDownLatch> newFileCountDownLatchCache = Maps.newConcurrentMap();

	private final Map<String, Integer> exchangeIdCache = Maps.newConcurrentMap();

	private final Integer exchangeStartId = 0;

	public SideCarSocketCacheManager(Project project) {
		this.project = project;
	}

	/**
	 * 缓存命令，等待用户在webview点击执行
	 */
	public void cacheCommand(String commandId, CancelCountDownLatch countDownLatch) {
		commandCache.put(commandId, countDownLatch);
	}

	/**
	 * 用户点击执行命令
	 */
	public void executeCommand(String commandId) {
		CountDownLatch countDownLatch = commandCache.get(commandId);
		if (countDownLatch != null) {
			countDownLatch.countDown();
			commandCache.remove(commandId);
		}
	}

	/**
	 * 用户点击取消命令
	 */
	public void cancelCommand(String commandId) {
		CancelCountDownLatch commandLatch = commandCache.get(commandId);
		CancelCountDownLatch processLatch = processCache.get(commandId);
		if (commandLatch != null) {
			commandLatch.setCancel(true);
			commandLatch.countDown();
			commandCache.remove(commandId);
		}
		if (processLatch != null) {
			processLatch.setCancel(true);
			processLatch.countDown();
			processCache.remove(commandId);
		}
	}

	/**
	 * 缓存ProcessHandler，以便处理用户取消命令
	 */
	public void cacheProcess(String commandId, CancelCountDownLatch countDownLatch) {
		processCache.put(commandId, countDownLatch);
	}


	/**
	 * 命令执行完成
	 */
	public void processCompleted(String commandId) {
		processCache.remove(commandId);
	}


	/**
	 * 缓存exchangeId
	 */
	public Integer getExchangeId(String sessionId) {
		Integer exchangeId = exchangeIdCache.get(sessionId);
		if (Objects.nonNull(exchangeId)) {
			exchangeIdCache.put(sessionId, exchangeId + 1);
			return exchangeId + 1;
		} else {
			exchangeIdCache.put(sessionId, exchangeStartId + 1);
			return exchangeStartId + 1;
		}

	}


	/**
	 * 缓存新建文件的CountDownLatch
	 */
	public void cacheNewFileCountDownLatch(String filePath, CountDownLatch countDownLatch) {
		newFileCountDownLatchCache.put(filePath, countDownLatch);
	}

	/**
	 * 获取新建文件的CountDownLatch
	 */
	public CountDownLatch getNewFileCountDownLatch(String filePath) {
		return newFileCountDownLatchCache.get(filePath);
	}

	/**
	 * 删除新建文件的CountDownLatch
	 */
	public void removeNewFileCountDownLatch(String filePath) {
		newFileCountDownLatchCache.remove(filePath);
	}


}
