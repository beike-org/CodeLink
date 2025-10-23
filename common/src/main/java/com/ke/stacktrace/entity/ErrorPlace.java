package com.ke.stacktrace.entity;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiManager;
import com.ke.utils.PsiUtils;
import lombok.Builder;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;


@Getter
@Builder
public class ErrorPlace {
	@NotNull
	private final String hyperlinkText;
	private final int lineNumber;
	private final boolean isProjectFile;
	@NotNull
	private final VirtualFile virtualFile;
	@NotNull
	private final Project project;

	public final @Nullable PsiFile getPsiFile() {
		return PsiManager.getInstance(this.project).findFile(this.virtualFile);
	}


	public final @Nullable PsiElement findContainingElement() {
		PsiFile psiFile = this.getPsiFile();
		if (psiFile == null) {
			return null;
		}
		Integer startOffset = PsiUtils.getLineStartOffset(psiFile, this.lineNumber);
		if (startOffset == null) {
			return null;
		}
		int errorPlaceOffset = startOffset;
		psiFile = this.getPsiFile();
		return psiFile != null ? psiFile.findElementAt(errorPlaceOffset) : null;

	}
}
