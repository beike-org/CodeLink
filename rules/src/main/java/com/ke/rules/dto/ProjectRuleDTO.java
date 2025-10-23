package com.ke.rules.dto;

import com.ke.rules.enums.RuleType;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
public class ProjectRuleDTO {
	private String name;        // 规则名称
	private String content;
	private RuleType type;
	//当类型为指定类型时，用户可以填regex
	private List<String> regex;
}
