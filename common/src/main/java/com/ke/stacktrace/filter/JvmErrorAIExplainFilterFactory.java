package com.ke.stacktrace.filter;


import com.intellij.execution.filters.Filter;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;

public class JvmErrorAIExplainFilterFactory implements ErrorAIExplainFilterFactory {

	@Override
	public Filter[] getDefaultFilters(@NotNull Project project) {
		return new Filter[]{new JvmErrorAIExplainFilter(project)};
	}
}
