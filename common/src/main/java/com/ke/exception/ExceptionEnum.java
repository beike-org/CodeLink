package com.ke.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Objects;

/**
 * @Author: zhangshaoxun001
 * @Date: 2022/12/6 17:18
 * @Version 1.0
 * @Description
 */
@AllArgsConstructor
@Getter
public enum ExceptionEnum {
	JSON_SERIALIZE_EXCEPTION(50001, "数据传输异常"),
	HTTP_REQUEST_EXCEPTION(50002, "请求异常:%s"),
	PROJECT_NOT_EXISTS_EXCEPTION(50003, "project不存在"),
	EDITOR_IS_NULL_EXCEPTION(50004, "editor为空"),
	FILE_NOT_EXISTS_EXCEPTION(50005, "文件不存在%s"),
	DIR_NOT_EXISTS_EXCEPTION(50006, "目录不存在%s"),
	DIR_CREATE_EXCEPTION(50007, "新目录创建异常%s"),
	BINARY_COMMAND_NOT_COMPATIBLE_EXCEPTION(50008, "%s未支持当前os arch"),
	BINARY_VERSION_EXCEPTION(50009, "%s");

	private final Integer errCode;

	private final String errMessage;

	public void asBusinessException(Object... params) {
		if (Objects.isNull(params) || params.length == 0) {
			throw new BusinessException(errCode, errMessage.replaceAll("%s", ""));
		}
		throw new BusinessException(errCode, String.format(errMessage, params));
	}


	public BusinessException getBusinessException() {
		return new BusinessException(errCode, errMessage);
	}
}
