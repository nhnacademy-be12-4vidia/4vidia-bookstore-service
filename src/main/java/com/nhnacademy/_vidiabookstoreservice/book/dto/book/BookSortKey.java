package com.nhnacademy._vidiabookstoreservice.book.dto.book;

public enum BookSortKey {
    PUBLISHED_DATE,
    PRICE_SALES,
    AVG_RATING;

    public static BookSortKey from(String raw) {
        if (raw == null) return PUBLISHED_DATE;
        return switch (raw) {
            case "publishedDate" -> PUBLISHED_DATE;
            case "priceSales" -> PRICE_SALES;
            case "avgRating" -> AVG_RATING;
            default -> PUBLISHED_DATE;
        };
    }

    public boolean isEsOnly() {
        return this == AVG_RATING;
    }
}
