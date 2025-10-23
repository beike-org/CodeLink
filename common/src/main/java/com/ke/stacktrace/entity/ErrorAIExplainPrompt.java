package com.ke.stacktrace.entity;


import com.ke.stacktrace.RunTimeErrorExplainPrompt;
import lombok.Builder;
import lombok.Getter;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Objects;

@Builder
@Getter
public class ErrorAIExplainPrompt {

	@NotNull
	private String tracebackText;

	@NotNull
	private List<CodePlace> codePlaces;

	@Nullable
	private String language;


	public String getPrompt() {
		if (codePlaces.isEmpty()) {
			return RunTimeErrorExplainPrompt.noCodePrompt
					.replace("{traceback}", tracebackText);
		}
		return RunTimeErrorExplainPrompt.basic
				.replace("{code}", StringUtils.join(codePlaces.stream().map(CodePlace::getText).iterator(), "\n"))
				.replace("{lang}", Objects.isNull(language) ? "" : language)
				.replace("{traceback}", tracebackText);
	}
}
