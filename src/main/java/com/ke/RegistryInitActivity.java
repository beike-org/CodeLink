package com.ke;

import com.intellij.ide.ApplicationInitializedListener;
import com.intellij.openapi.application.impl.ApplicationInfoImpl;
import kotlin.Unit;
import kotlin.coroutines.Continuation;
import kotlinx.coroutines.CoroutineScope;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * @Author: zhangshaoxun001
 * @Date: 2025/5/14 14:42
 * @Description 处理2025版本JCEF远程连接问题,导致NPE无法打开webview的情况
 */
public class RegistryInitActivity implements ApplicationInitializedListener {

	@Override
	public void componentsInitialized() {
		setEnv();
		ApplicationInitializedListener.super.componentsInitialized();
	}

	public @Nullable Object execute(@NotNull CoroutineScope asyncScope, @NotNull Continuation<? super Unit> $completion) {
		setEnv();
		return Unit.INSTANCE;
	}

	public @Nullable Object execute(@NotNull Continuation<? super Unit> continuation) {
		setEnv();
		return Unit.INSTANCE;
	}

	private void setEnv() {
		if (ApplicationInfoImpl.getInstance().getMajorVersion().compareTo("2025") >= 0) {
			System.setProperty("jcef.remote.enabled", "false");
		}
	}
}
