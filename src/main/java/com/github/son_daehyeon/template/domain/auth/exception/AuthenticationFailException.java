package com.github.son_daehyeon.template.domain.auth.exception;

import com.github.son_daehyeon.template.common.api.exception.ApiException;
import org.springframework.http.HttpStatus;

public class AuthenticationFailException extends ApiException {

    public AuthenticationFailException() {

        super(HttpStatus.UNAUTHORIZED, "인증에 실패하였습니다.");
    }
}