package com.nhnacademy._vidiabookstoreservice.point.exception.notfound;

import com.nhnacademy._vidiabookstoreservice.global.exception.NotFoundException;

public class PointDetailNotFoundException extends NotFoundException {
    public PointDetailNotFoundException(Long orderId) {
        super("해당 주문에 사용된 포인트 내역을 찾을 수 없습니다." + orderId);
    }
}
