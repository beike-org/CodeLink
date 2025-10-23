package com.ke.utils;

import com.intellij.openapi.diagnostic.Logger;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.net.URLConnection;

/**
 * @Author: zhangshaoxun001
 * @Date: 2024/1/10 18:37
 * @Version 1.0
 * @Description
 */
public class UrlUtil {

    private static final Logger logger = Logger.getInstance(UrlUtil.class);

    /**
     * 判断url是否合法
     */
    public static boolean isValidURL(String urlStr) {
        try {
            new URL(urlStr);
            return true;
        } catch (MalformedURLException e) {
            return false;
        }
    }


    /**
     * 根据url获取数据流
     */
    public static byte[] getBytesFromUrl(String urlStr) {
        try {
            URI uri = new URI(urlStr);
            URL url = uri.toURL();
            URLConnection connection = url.openConnection();
            connection.connect();
            InputStream inputStream = connection.getInputStream();

            // 2. 将图片数据读取到字节数组
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            byte[] buffer = new byte[4096];
            int bytesRead;
            while ((bytesRead = inputStream.read(buffer)) != -1) {
                byteArrayOutputStream.write(buffer, 0, bytesRead);
            }
            return byteArrayOutputStream.toByteArray();
        } catch (Exception e) {
            logger.warn(String.format("获取%s数据流失败", urlStr), e);
        }
        return null;
    }
}
