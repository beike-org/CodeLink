package com.ke.rules.dto;


import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
public class RuleInfoDTO {

	private String globalRule;

	private List<ProjectRuleDTO> projectRule;
}
