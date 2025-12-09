package com.nhnacademy._vidiabookstoreservice.order.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.nhnacademy._vidiabookstoreservice.order.domain.Payment;
import com.nhnacademy._vidiabookstoreservice.order.dto.payment.request.PaymentCreateRequest;
import com.nhnacademy._vidiabookstoreservice.order.dto.payment.response.PaymentCancelResponse;
import com.nhnacademy._vidiabookstoreservice.order.dto.payment.response.PaymentResponse;
import com.nhnacademy._vidiabookstoreservice.order.dto.payment.response.TossPaymentResponse;
import com.nhnacademy._vidiabookstoreservice.order.exception.PaymentConfirmException;
import com.nhnacademy._vidiabookstoreservice.order.repository.PaymentRepository;
import com.nhnacademy._vidiabookstoreservice.order.service.PaymentService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Map;

@Service
@Transactional
public class TossPaymentServiceImpl implements PaymentService<TossPaymentResponse> {

    @Value("${toss.secretKey}")
    private String API_SECRET_KEY;

    private final ObjectMapper objectMapper;


    private final PaymentRepository paymentRepository;

    TossPaymentServiceImpl(ObjectMapper objectMapper, PaymentRepository paymentRepository) {
        this.objectMapper = objectMapper;
        this.paymentRepository = paymentRepository;
    }

    private static final String TOSS_CONFIRM_URL = "https://api.tosspayments.com/v1/payments/confirm";
    private static final String TOSS_URL = "https://api.tosspayments.com/v1/payments/";

    @Override
    @Transactional(readOnly = true)
    public PaymentResponse getPayment(long orderId) {
        Payment payment = paymentRepository.findPaymentByOrder_orderId(orderId);

        return PaymentResponse.from(payment);
    }

    @Override
    public PaymentResponse savePayment(PaymentCreateRequest paymentCreateRequest) {
        Payment payment = Payment.builder()
                .order(paymentCreateRequest.order())
                .payStatus(paymentCreateRequest.payStatus())
                .payMethod(paymentCreateRequest.payMethod())
                .amount(paymentCreateRequest.amount())
                .paymentKey(paymentCreateRequest.paymentKey())
                .sendOrderId(paymentCreateRequest.sendOrderId())
                .build();

        paymentRepository.save(payment);

        return PaymentResponse.from(payment);
    }

    @Override
    public PaymentCancelResponse getPaymentKey(long orderId) {
        Payment payment = paymentRepository.findPaymentByOrder_orderId(orderId);

        return PaymentCancelResponse.from(payment);
    }

    @Override
    public TossPaymentResponse confirmPayment(String paymentKey, String orderId, long amount) {
        Map<String, Object> requestData = Map.of(
                "paymentKey", paymentKey,
                "orderId", orderId,
                "amount", amount
        );

        Map<String, Object> response = null;
        try {
            response = sendRequest(requestData, API_SECRET_KEY, TOSS_CONFIRM_URL);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        if (response.containsKey("code")) {
            throw new PaymentConfirmException(response.toString());
        }

        return objectMapper.convertValue(response, TossPaymentResponse.class);
    }

    @Override
    public TossPaymentResponse cancelPayment(String paymentKey, String reason, long amount) {
        Map<String, Object> requestData = Map.of(
                "cancelReason", reason,
                "cancelAmount", amount
        );
        /* TODO 가상계좌 사용시 환불 계좌 "refundReceiveAccount"
        bank 필수 · string - 취소 금액을 환불받을 계좌의 은행 코드입니다. 은행 코드와 증권사 코드를 참고하세요.
        accountNumber 필수 · string - 취소 금액을 환불받을 계좌의 계좌번호입니다. - 없이 숫자만 넣어야 합니다. 최대 길이는 20자입니다.
        holderName 필수 · string - 취소 금액을 환불받을 계좌의 예금주입니다. 최대 길이는 60자입니다.
         */
        Map<String, Object> response = null;
        try {
            response = sendRequest(requestData, API_SECRET_KEY, TOSS_URL + paymentKey + "/cancel");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        return  objectMapper.convertValue(response, TossPaymentResponse.class);
    }

    private Map<String, Object> sendRequest(Map<String, Object> requestData, String secretKey, String urlString) throws IOException {
        HttpURLConnection connection = createConnection(secretKey, urlString);

        try (OutputStream os = connection.getOutputStream()) {
            objectMapper.writeValue(os, requestData);
        }

        try (InputStream responseStream = connection.getResponseCode() == 200 ? connection.getInputStream() : connection.getErrorStream();
             Reader reader = new InputStreamReader(responseStream, StandardCharsets.UTF_8)) {

            return objectMapper.readValue(reader, new com.fasterxml.jackson.core.type.TypeReference<>() {});

        } catch (Exception e) {
            return Map.of("code", "COMMUNICATION_ERROR", "message", e.getMessage());
        }
    }

    private HttpURLConnection createConnection(String secretKey, String urlString) throws IOException {
        URL url = new URL(urlString);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestProperty("Authorization", "Basic " + Base64.getEncoder().encodeToString((secretKey + ":").getBytes(StandardCharsets.UTF_8)));
        connection.setRequestProperty("Content-Type", "application/json");
        connection.setRequestMethod("POST");
        connection.setDoOutput(true);
        return connection;
    }


}
