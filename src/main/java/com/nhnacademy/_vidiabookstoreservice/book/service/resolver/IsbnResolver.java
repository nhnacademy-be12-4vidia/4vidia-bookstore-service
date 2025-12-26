package com.nhnacademy._vidiabookstoreservice.book.service.resolver;

import com.nhnacademy._vidiabookstoreservice.book.exception.invalid.BookIsbnInvalidException;
import org.apache.commons.validator.routines.ISBNValidator;

public final class IsbnResolver {

    private static final ISBNValidator ISBN_VALIDATOR = ISBNValidator.getInstance(true);

    public static String toIsbn13(String raw) {
        if (raw == null) {
            throw new BookIsbnInvalidException();
        }

        // 공백 제거 및 하이픈 제거 후 검증
        String cleaned = raw.trim().replace("-", "");
        String result = ISBN_VALIDATOR.validate(cleaned);

        if (result == null) {
            throw new BookIsbnInvalidException();
        }

        return result;
    }
}
