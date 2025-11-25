package com.nhnacademy._vidiabookstoreservice.order.domain.converters;

import com.nhnacademy.order.domain.DeliveryStatus;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter(autoApply = true)
public class DeliveryStatusConverter implements AttributeConverter<DeliveryStatus, Integer> {
    @Override
    public Integer convertToDatabaseColumn(DeliveryStatus attribute) {
        if (attribute == null) return null;
        return attribute.getCode();
    }

    @Override
    public DeliveryStatus convertToEntityAttribute(Integer dbStatus) {
        if (dbStatus == null) return null;
        return DeliveryStatus.fromCode(dbStatus);
    }
}
