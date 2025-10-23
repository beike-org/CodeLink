package com.ke.stacktrace.render;

import com.intellij.codeInsight.hints.presentation.IconPresentation;
import com.intellij.codeInsight.hints.presentation.PresentationRenderer;
import com.intellij.execution.filters.Filter;
import com.intellij.execution.impl.InlayProvider;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.EditorCustomElementRenderer;
import com.intellij.openapi.editor.impl.EditorImpl;
import com.ke.stacktrace.RuntimeErrorAIExplanationProvider;
import com.ke.stacktrace.entity.SelectedTraceback;
import com.ke.stacktrace.meta.StackTraceInfoProvider;
import com.ke.utils.IconUtil;
import org.apache.commons.collections.CollectionUtils;

import java.util.List;

import static com.ke.stacktrace.meta.StackTraceInfoProvider.STACK_TRACE_INFO_PROVIDER;

public class ConsoleInLayProvider extends Filter.ResultItem implements InlayProvider {

	private final int startOffset;

	public ConsoleInLayProvider(int offset) {
		super(offset, offset, null);
		this.startOffset = offset;
	}


	@Override
	public EditorCustomElementRenderer createInlayRenderer(Editor editor) {

		try {

			IconPresentation iconPresentation = new IconPresentation(IconUtil.getStackTraceIcon(), editor.getComponent());

			List<StackTraceInfoProvider> stackTraceInfoProviderList = STACK_TRACE_INFO_PROVIDER.getExtensionList();
			if (CollectionUtils.isEmpty(stackTraceInfoProviderList)) {
				return null;
			}
			SelectedTraceback selectTraceback = stackTraceInfoProviderList.get(0).getSelectTraceback(editor, startOffset);

			return new PresentationRenderer(new DelegateClickPresentation(iconPresentation, () -> {

				new RuntimeErrorAIExplanationProvider(
						editor.getProject(),
						editor,
						selectTraceback).run();
			}, (EditorImpl) editor));

		} catch (NoSuchMethodError e) {
			return null;
		}

	}


}
