package com.nhnacademy._vidiabookstoreservice.book.dto.event;

import java.time.LocalDateTime;

public record DescriptionImageUploadedEvent(
    String imageUrl,
    LocalDateTime uploadedAt
) {
}
