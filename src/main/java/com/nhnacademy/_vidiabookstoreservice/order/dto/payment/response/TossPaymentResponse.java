package com.nhnacademy._vidiabookstoreservice.order.dto.payment.response;

import java.util.List;

public record TossPaymentResponse(
       String version,
       String paymentKey,
       String type,
       String orderId,
       String orderName,
       String mId,
       String currency,
       String method,
       long totalAmount,
       long balanceAmount,
       String status,
       String requestedAt, //yyyy-MM-dd'T'HH:mm:ss±hh:mm
       String approvedAt,
       Boolean useEscrow,
       String lastTransactionKey,
       long suppliedAmount,
       String vat,
       Boolean cultureExpense,
       long taxFreeAmount,
       int taxExemptionAmount,
       List<Cancel> cancels,
       Boolean isPartialCancelable,
       Card card,
       VirtualAccount virtualAccount,
       String secret,
       MobilePhone mobilePhone,
       GiftCertificate giftCertificate,
       Transfer transfer,
       Object metadata,
       Receipt receipt,
       Checkout checkout,
       EasyPay easyPay,
       String country,
       Failure failure,
       CashReceipt cashReceipt,
       List<CashReceipts> cashReceipts,
       Discount discount
) {
    public record Card(
            Long amount,
            String issuerCode,
            String acquirerCode,
            String number,
            Integer installmentPlanMonths,
            String approveNo,
            Boolean useCardPoint,
            String cardType,
            String ownerType,
            String acquireStatus,
            Boolean isInterestFree,
            String interestPayer
    ) { }

    public record Cancel(
            Long cancelAmount,
            String cancelReason,
            Long taxFreeAmount,
            Integer taxExemptionAmount,
            Long refundableAmount,
            Long cardDiscountAmount,
            Long transferDiscountAmount,
            Long easyPayDiscountAmount,
            String canceledAt, //yyyy-MM-dd'T'HH:mm:ss±hh:mm
            String transactionKey,
            String receiptKey,
            String cancelStatus,
            String cancelRequestId
    ) { }

    public record CashReceipt(
            String type,
            String receiptKey,
            String issueNumber,
            String receiptUrl,
            Long amount,
            Long taxFreeAmount
    ) { }

    public record CashReceipts(
            String receiptKey,
            String orderId,
            String orderName,
            String type,
            String issueNumber,
            String receiptUrl,
            String businessNumber,
            String transactionType,
            String amount,
            String taxFreeAmount,
            String issueStatus,
            Failure failure,
            String customerIdentityNumber,
            String requestedAt
    ) { }

    public record Failure(
            String code,
            String message
    ) { }

    public record Checkout(
            String url
    ) { }

    public record Discount(
            Integer amount
    ) { }

    public record EasyPay(
            String provider,
            Long amount,
            Long discountAmount
    ) { }

    public record GiftCertificate(
            String approveNo,
            String settlementStatus
    ) { }

    public record MobilePhone(
            String customerMobilePhone,
            String settlementStatus,
            String receiptUrl
    ) { }

    public record Receipt(
            String url
    ) { }

    public record VirtualAccount(
            String accountType,
            String accountNumber,
            String bankCode,
            String customerName,
            String depositorName,
            String dueDate,
            String refundStatus,
            Boolean expired,
            String settlementStatus,
            RefundReceiveAccount refundReceiveAccount
    ) { }

    public record RefundReceiveAccount(
            String bankCode,
            String accountNumber,
            String holderName
    ) { }

    public record Transfer(
            String bankCode,
            String settlementStatus
    ) { }
}

