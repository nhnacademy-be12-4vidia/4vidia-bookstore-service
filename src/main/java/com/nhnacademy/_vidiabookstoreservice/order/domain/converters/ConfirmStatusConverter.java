package com.nhnacademy._vidiabookstoreservice.order.domain.converters;

import com.nhnacademy._vidiabookstoreservice.order.domain.enums.ConfirmStatus;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter(autoApply = true)
public class ConfirmStatusConverter implements AttributeConverter<ConfirmStatus, Integer> {
    @Override
    public Integer convertToDatabaseColumn(ConfirmStatus attribute) {
        if (attribute == null) return null;
        return attribute.getCode();
    }

    @Override
    public ConfirmStatus convertToEntityAttribute(Integer dbStatus) {
        if (dbStatus == null) return null;
        return ConfirmStatus.fromCode(dbStatus);
    }
}
