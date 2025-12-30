package com.nhnacademy._vidiabookstoreservice.book.domain.converters;

import com.nhnacademy._vidiabookstoreservice.book.domain.enums.ImageType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ImageTypeConverterTest {

    private final ImageTypeConverter converter = new ImageTypeConverter();

    @Test
    @DisplayName("Enum을 DB 컬럼 값(Integer)으로 변환 성공")
    void convertToDatabaseColumn() {
        ImageType status = ImageType.THUMBNAIL;

        Integer result = converter.convertToDatabaseColumn(status);

        assertThat(result).isEqualTo(status.getCode());
    }

    @Test
    @DisplayName("DB 컬럼 값(Integer)을 Enum으로 변환 성공")
    void convertToEntityAttribute() {
        Integer dbCode = 1;

        ImageType result = converter.convertToEntityAttribute(dbCode);

        assertThat(result).isEqualTo(ImageType.DETAIL);
    }

    @Test
    @DisplayName("Null 입력 시 Null 반환 확인")
    void convertNull() {
        assertThat(converter.convertToDatabaseColumn(null)).isNull();
        assertThat(converter.convertToEntityAttribute(null)).isNull();
    }

    @Test
    @DisplayName("존재하지 않는 코드 입력 시 예외 발생 여부 확인")
    void convertInvalidCode() {
        Integer invalidCode = 999;

        assertThatThrownBy(() -> converter.convertToEntityAttribute(invalidCode))
                .isInstanceOf(IllegalArgumentException.class);
    }
}