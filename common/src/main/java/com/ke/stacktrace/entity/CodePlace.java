package com.ke.stacktrace.entity;

import com.intellij.openapi.vfs.VirtualFile;
import kotlin.jvm.internal.Intrinsics;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;


@AllArgsConstructor
@Getter
@Builder
public class CodePlace {
	private final int lineStart;
	private final int lineFinish;
	@NotNull
	private final String text;
	@NotNull
	private final VirtualFile thisVirtualFile;

	public final boolean containsLineNumber(int lineNumber, @NotNull VirtualFile virtualFile) {
		return this.lineStart <= lineNumber && lineNumber <= this.lineFinish && Intrinsics.areEqual(this.thisVirtualFile, virtualFile);
	}

}