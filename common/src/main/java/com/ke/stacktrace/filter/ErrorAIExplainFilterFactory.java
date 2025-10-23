package com.ke.stacktrace.filter;

import com.intellij.execution.filters.Filter;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;

/**
 * 自定义的拓展点，用于扩展错误解释的过滤器以便实现不同的语言栈之间的兼容
 */
public interface ErrorAIExplainFilterFactory {

	Filter[] getDefaultFilters(@NotNull Project project);

}