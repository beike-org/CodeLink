package com.ke.utils;

/**
 * @Author: zhangshaoxun001
 * @Date: 2023/10/24 16:16
 * @Version 1.0
 * @Description
 */
public class H5Util {

	public static String createALink(String url, String displayText) {
		return "<html><a href=\"" + url + "\">" + displayText + "</a><html>";
	}
}
