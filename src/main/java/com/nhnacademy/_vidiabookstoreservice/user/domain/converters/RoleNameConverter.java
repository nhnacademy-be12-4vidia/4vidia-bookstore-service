package com.nhnacademy._vidiabookstoreservice.user.domain.converters;

import com.nhnacademy._vidiabookstoreservice.user.domain.enums.UserRole;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;



@Converter(autoApply = true)
public class RoleNameConverter implements AttributeConverter<UserRole, Integer> {
    @Override
    public Integer convertToDatabaseColumn(UserRole role){
        if(role == null){
            return null;
        }
        return role.getCode();
    }

    @Override
    public UserRole convertToEntityAttribute(Integer dbData){
        if(dbData == null){
            return null;
        }
        return UserRole.of(dbData);
    }
}
