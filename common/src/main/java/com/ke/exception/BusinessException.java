package com.ke.exception;

/**
 * @Author: zhangshaoxun001
 * @Date: 2022/12/6 17:12
 * @Version 1.0
 * @Description
 */
public class BusinessException extends RuntimeException{

    final Integer code;

    public BusinessException(String message) {
        super(message);
        this.code = 40001;
    }

    public BusinessException(Integer code, String message) {
        super(message);
        this.code = code;
    }

    public BusinessException(Integer code, String message, Throwable throwable) {
        super(message, throwable);
        this.code = code;

    }

    public Integer getCode() {
        return this.code;
    }
}

