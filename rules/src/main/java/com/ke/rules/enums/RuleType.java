package com.ke.rules.enums;

import com.ke.Bundle;
import lombok.Getter;

@Getter
public enum RuleType {
	MANUAL(Bundle.get("rule.type.manual")),
	ALWAYS(Bundle.get("rule.type.always")),
	SPECIFIED_TYPE(Bundle.get("rule.type.specifiedType"));

	private final String displayName;

	RuleType(String displayName) {
		this.displayName = displayName;
	}

	@Override
	public String toString() {
		return displayName;
	}
}