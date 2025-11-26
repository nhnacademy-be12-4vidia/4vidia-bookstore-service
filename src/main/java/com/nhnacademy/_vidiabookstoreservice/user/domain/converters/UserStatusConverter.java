package com.nhnacademy._vidiabookstoreservice.user.domain.converters;


import com.nhnacademy._vidiabookstoreservice.user.domain.enums.UserStatus;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter(autoApply = true)
public class UserStatusConverter implements AttributeConverter<UserStatus, Integer> {
    @Override
    public Integer convertToDatabaseColumn(UserStatus status) {
        if(status== null){
            return null;
        }
        return status.getCode();
    }

    @Override
    public UserStatus convertToEntityAttribute(Integer dbData){
        if(dbData == null){
            return null;
        }
        return UserStatus.of(dbData);
    }
}
