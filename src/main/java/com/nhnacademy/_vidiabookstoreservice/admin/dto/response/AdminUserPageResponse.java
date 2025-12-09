package com.nhnacademy._vidiabookstoreservice.admin.dto.response;

import java.util.List;

public record AdminUserPageResponse(
        List<AdminUserResponse> content,
        int number,
        int size,
        long totalElements,
        int totalPages,
        boolean first,
        boolean last,
        boolean empty
) {
    public static AdminUserPageResponse from(org.springframework.data.domain.Page<AdminUserResponse> page) {
        return new AdminUserPageResponse(
                page.getContent(),
                page.getNumber(),
                page.getSize(),
                page.getTotalElements(),
                page.getTotalPages(),
                page.isFirst(),
                page.isLast(),
                page.isEmpty()
        );
    }
}
