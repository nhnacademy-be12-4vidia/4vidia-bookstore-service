package com.nhnacademy._vidiabookstoreservice.order.dto.order.request;

import java.util.List;

public record CouponUseRequest(
        Long orderId,
        List<Long> couponIds
) {
}


//이메일, 비번 쳐서 로그인 버튼
//-> 1. 이메일, 비번 맞는지 검사
//-> 2. 맞으면 이메일로 상태검사
//-> 3. 활성상태면 메인?
//-> 4. 휴면이면 휴면인증페이지로(로그인 성공하면 현재로그)
