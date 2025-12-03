package com.nhnacademy._vidiabookstoreservice.point.domain.converters;

import com.nhnacademy._vidiabookstoreservice.point.domain.enums.PointReason;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter(autoApply = true)
public class PointReasonConverter implements AttributeConverter<PointReason, Integer> {

    @Override
    public Integer convertToDatabaseColumn(PointReason pointReason) {
        if(pointReason == null) {
            return null;
        }
        return pointReason.getCode();
    }

    @Override
    public PointReason convertToEntityAttribute(Integer dbData) {
        if(dbData == null) {
            return null;
        }
        return PointReason.of(dbData);
    }
}
