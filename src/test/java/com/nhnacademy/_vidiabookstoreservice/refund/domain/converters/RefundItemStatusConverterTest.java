package com.nhnacademy._vidiabookstoreservice.refund.domain.converters;

import com.nhnacademy._vidiabookstoreservice.refund.domain.enums.RefundItemStatus;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

public class RefundItemStatusConverterTest {
    private final RefundItemStatusConverter converter = new RefundItemStatusConverter();

    @Test
    @DisplayName("Enum을 DB 컬럼 값으로 변환 성공")
    void convertToDatabaseColumn() {
        RefundItemStatus status = RefundItemStatus.PROCESS;

        Integer result = converter.convertToDatabaseColumn(status);

        assertThat(result).isEqualTo(status.getCode());
    }

    @Test
    @DisplayName("DB 컬럼 값을 Enum으로 변환 성공")
    void convertToEntityAttribute(){
        Integer dbCode = 1;
        RefundItemStatus result = converter.convertToEntityAttribute(dbCode);
        assertThat(result).isEqualTo(RefundItemStatus.APPROVED);
    }

    @Test
    @DisplayName("Null 입력 시 Null 반환 확인")
    void convertNull() {
        assertThat(converter.convertToDatabaseColumn(null)).isNull();
        assertThat(converter.convertToEntityAttribute(null)).isNull();
    }

    @Test
    @DisplayName("존재하지 않는 코드 입력 시 예외 발생")
    void convertInvalidCode(){
        Integer invalidCode = 111;

        assertThatThrownBy(() -> converter.convertToEntityAttribute(invalidCode))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
