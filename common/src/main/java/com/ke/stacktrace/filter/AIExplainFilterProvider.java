package com.ke.stacktrace.filter;

import com.intellij.execution.filters.ConsoleFilterProvider;
import com.intellij.execution.filters.Filter;
import com.intellij.openapi.extensions.ExtensionPointName;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

// 根据语言栈注册不同的错误解释过滤器
public class AIExplainFilterProvider implements ConsoleFilterProvider {

	// 拓展点绑定：https://plugins.jetbrains.com/docs/intellij/plugin-configuration-file.html#idea-plugin__extensionPoints__extensionPoint
	private static final ExtensionPointName<ErrorAIExplainFilterFactory> EP_NAME =
			ExtensionPointName.create("com.ke.codelink.errorAIExplainFilterFactory");

	@Override
	public Filter[] getDefaultFilters(@NotNull Project project) {
		List<Filter> filters = new ArrayList<>();

		for (ErrorAIExplainFilterFactory extension : EP_NAME.getExtensionList()) {
			filters.addAll(List.of(extension.getDefaultFilters(project)));
		}
		return filters.toArray(new Filter[0]);
	}
}
