package com.nhnacademy._vidiabookstoreservice.user.domain.converters;


import com.nhnacademy._vidiabookstoreservice.user.domain.enums.GradeName;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;


@Converter(autoApply = true)
public class GradeNameConverter implements AttributeConverter<GradeName, Integer> {
    @Override
    public Integer convertToDatabaseColumn(GradeName gradeName){
        if(gradeName == null){
            return null;
        }
        return gradeName.getCode();
    }

    @Override
    public GradeName convertToEntityAttribute(Integer dbData){
        if(dbData == null){
            return null;
        }
        return GradeName.of(dbData);
    }
}
