package com.nhnacademy._vidiabookstoreservice.refund.domain.converters;

import com.nhnacademy._vidiabookstoreservice.refund.domain.enums.RefundItemStatus;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter(autoApply = true)
public class RefundItemStatusConverter implements AttributeConverter<RefundItemStatus, Integer> {

    @Override
    public Integer convertToDatabaseColumn(RefundItemStatus refundStatus) {
        if (refundStatus == null) return null;
        return refundStatus.getCode();
    }

    @Override
    public RefundItemStatus convertToEntityAttribute(Integer dbStatus) {
        if (dbStatus == null) return null;
        return RefundItemStatus.fromCode(dbStatus);
    }
}