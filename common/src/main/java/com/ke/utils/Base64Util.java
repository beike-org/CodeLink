package com.ke.utils;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

/**
 * Base64工具类
 */
public class Base64Util {

	/**
	 * 编码
	 */
	public static String encode(String str) {
		return str == null ? null : Base64.getEncoder().encodeToString(str.getBytes(StandardCharsets.UTF_8));
	}

	/**
	 * 解码
	 */
	public static String decode(String base64Str) {
		return base64Str == null ? null : new String(Base64.getDecoder().decode(base64Str), StandardCharsets.UTF_8);
	}
}