package com.nhnacademy._vidiabookstoreservice.refund.domain;

import com.nhnacademy._vidiabookstoreservice.refund.dto.RefundStatus;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter(autoApply = true)
public class RefundStatusConverter implements AttributeConverter<RefundStatus, Integer> {

    @Override
    public Integer convertToDatabaseColumn(RefundStatus refundStatus) {
        if (refundStatus == null) return null;
        return refundStatus.getCode();
    }

    @Override
    public RefundStatus convertToEntityAttribute(Integer dbStatus) {
        if (dbStatus == null) return null;
        return RefundStatus.fromCode(dbStatus);
    }
}