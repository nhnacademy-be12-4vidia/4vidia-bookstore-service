package com.nhnacademy._vidiabookstoreservice.order.domain.converters;

import com.nhnacademy.order.domain.OrderStatus;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter(autoApply = true)
public class OrderStatusConverter implements AttributeConverter<OrderStatus, Integer> {
    @Override
    public Integer convertToDatabaseColumn(OrderStatus attribute) {
        if (attribute == null) return null;
        return attribute.getCode();
    }

    @Override
    public OrderStatus convertToEntityAttribute(Integer dbStatus) {
        if (dbStatus == null) return null;
        return OrderStatus.fromCode(dbStatus);
    }
}
