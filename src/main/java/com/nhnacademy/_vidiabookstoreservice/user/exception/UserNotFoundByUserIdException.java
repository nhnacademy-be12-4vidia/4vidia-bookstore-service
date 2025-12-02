package com.nhnacademy._vidiabookstoreservice.user.exception;

import com.nhnacademy._vidiabookstoreservice.global.exception.NotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.NOT_FOUND) // 404
public class UserNotFoundByUserIdException extends NotFoundException {
    public UserNotFoundByUserIdException(Long userId) {
        super("존재하지 않는 회원입니다. 회원pk : " + userId);
    }
}
