package com.nhnacademy._vidiabookstoreservice.order.service;

import com.nhnacademy._vidiabookstoreservice.order.dto.order.response.OrderItemResponse;

public interface OrderItemService{

    OrderItemResponse getByOrderItemId(Long orderItemId);

}
