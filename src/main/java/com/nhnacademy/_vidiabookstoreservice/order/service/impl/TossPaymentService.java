package com.nhnacademy._vidiabookstoreservice.order.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.nhnacademy.order.domain.dto.TossPayment;
import com.nhnacademy.order.exception.PaymentConfirmException;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

@Service
public class TossPaymentService {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    ObjectMapper objectMapper = new ObjectMapper(); //TODO 오브젝트매퍼 받아오기?

    @Value("${toss.secretKey}")
    private String API_SECRET_KEY;

    private static final String TOSS_CONFIRM_URL = "https://api.tosspayments.com/v1/payments/confirm";


    public TossPayment confirmPayment(String paymentKey, String orderId, long amount) throws IOException {
        JSONObject requestData = new JSONObject();
        requestData.put("paymentKey", paymentKey);
        requestData.put("orderId", orderId);
        requestData.put("amount", amount);

        JSONObject response = sendRequest(requestData, API_SECRET_KEY, TOSS_CONFIRM_URL);

        logger.info("Response Data: {}", response); // TODO 확인용. 지우기

        if (response.containsKey("error")) {
            throw new PaymentConfirmException(response.toJSONString());
        }

        TossPayment tossPayment = objectMapper.convertValue(response, TossPayment.class);

       return tossPayment;
    }

    private JSONObject sendRequest(JSONObject requestData, String secretKey, String urlString) throws IOException {
        HttpURLConnection connection = createConnection(secretKey, urlString);
        try (OutputStream os = connection.getOutputStream()) {
            os.write(requestData.toString().getBytes(StandardCharsets.UTF_8));
        }

        try (InputStream responseStream = connection.getResponseCode() == 200 ? connection.getInputStream() : connection.getErrorStream();
             Reader reader = new InputStreamReader(responseStream, StandardCharsets.UTF_8)) {
            return (JSONObject) new JSONParser().parse(reader);
        } catch (Exception e) {
            logger.error("Error reading response", e);
            JSONObject errorResponse = new JSONObject();
            errorResponse.put("error", "Error reading response");
            return errorResponse;
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
