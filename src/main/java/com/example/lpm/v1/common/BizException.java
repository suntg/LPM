package com.example.lpm.v1.common;

import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
public class BizException extends BaseException {

    private static final long serialVersionUID = 1L;

    private static final int DEFAULT_ERROR_CODE = 510;

    public BizException(String errorMessage) {
        super(DEFAULT_ERROR_CODE, errorMessage);
    }

    public BizException(int errorCode, String errorMessage) {
        super(errorCode, errorMessage);
    }

    public BizException(String errorMessage, Throwable e) {
        super(errorMessage, e);
    }

    public BizException(int errorCode, String errorMessage, Throwable e) {
        super(errorCode, errorMessage, e);
    }
}
