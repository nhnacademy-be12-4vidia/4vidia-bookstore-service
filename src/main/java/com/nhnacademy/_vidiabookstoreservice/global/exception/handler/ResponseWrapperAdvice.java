package com.nhnacademy._vidiabookstoreservice.global.exception.handler;

import com.nhnacademy._vidiabookstoreservice.global.dto.ApiResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.MethodParameter;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyAdvice;

@Slf4j
@RestControllerAdvice(basePackages = "com.nhnacademy._vidiabookstoreservice")
public class ResponseWrapperAdvice implements ResponseBodyAdvice<Object> {
    private static final String INTERNAL_API_PREFIX = "/internal";

    @Override
    public boolean supports(MethodParameter returnType, Class<? extends HttpMessageConverter<?>> converterType) {
        return !returnType.getParameterType().equals(String.class);
    }

    @Override
    public Object beforeBodyWrite(Object body, MethodParameter returnType, MediaType selectedContentType,
                                  Class<? extends HttpMessageConverter<?>> selectedConverterType,
                                  ServerHttpRequest request, ServerHttpResponse response) {
        String path = request.getURI().getPath();

        if (path.startsWith(INTERNAL_API_PREFIX)) {
            return body;
        }
        if (body == null) {
            return ApiResponse.success(null);
        }
        // 이미 ApiResponse라면 (에러 핸들러에서 온 경우) 그대로 내보냄
        if (body instanceof ApiResponse) {
            return body;
        }


        // 정상 응답 DTO라면 ApiResponse.success로 감싸서 내보냄
        return ApiResponse.success(body);
    }
}