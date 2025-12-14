package com.nhnacademy._vidiabookstoreservice.book.domain.converters;

import com.nhnacademy._vidiabookstoreservice.book.domain.enums.SummaryStatus;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter(autoApply = true)
public class SummaryStatusConverter implements AttributeConverter<SummaryStatus, Integer> {

    @Override
    public Integer convertToDatabaseColumn(SummaryStatus attribute) {
        if (attribute == null) {
            return null;
        }
        return attribute.getCode();
    }

    @Override
    public SummaryStatus convertToEntityAttribute(Integer dbData) {
        if (dbData == null) {
            return null;
        }
        return SummaryStatus.of(dbData);
    }
}
