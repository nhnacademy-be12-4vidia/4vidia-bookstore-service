package com.nhnacademy._vidiabookstoreservice.user.service;

import com.nhnacademy._vidiabookstoreservice.user.domain.User;

public interface UserService {

    User getByUserId(Long userId);

}
