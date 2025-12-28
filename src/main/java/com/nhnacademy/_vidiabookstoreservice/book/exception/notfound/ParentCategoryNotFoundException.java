package com.nhnacademy._vidiabookstoreservice.book.exception.notfound;

import com.nhnacademy._vidiabookstoreservice.book.exception.BookErrorCode;
import com.nhnacademy._vidiabookstoreservice.global.exception.BaseException;

public class ParentCategoryNotFoundException extends BaseException {
    public ParentCategoryNotFoundException(String parentCode) {
        super(BookErrorCode.PARENT_CATEGORY_NOT_FOUND, "상위 카테고리를 찾을 수 없습니다. ParentCode: " + parentCode);
    }
}
