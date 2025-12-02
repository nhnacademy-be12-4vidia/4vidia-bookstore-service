package com.nhnacademy._vidiabookstoreservice.user.dto.grade.response;


import lombok.Builder;

@Builder
public record GradeResponse(
        String gradeName,
        Integer pointRate

)
        {
}
