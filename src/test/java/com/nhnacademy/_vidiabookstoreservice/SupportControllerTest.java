package com.nhnacademy._vidiabookstoreservice;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.nhnacademy._vidiabookstoreservice.book.repository.search.BookSearchRepository;
import com.nhnacademy._vidiabookstoreservice.cart.service.scheduler.CartSyncScheduler;
import com.nhnacademy._vidiabookstoreservice.order.config.RabbitMqInitializer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.restdocs.AutoConfigureRestDocs;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.restdocs.RestDocumentationContextProvider;
import org.springframework.restdocs.RestDocumentationExtension;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.filter.CharacterEncodingFilter;

import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.documentationConfiguration;

// 공통 설정 모음
@Transactional
@ActiveProfiles("test")
@ExtendWith(RestDocumentationExtension.class)
@AutoConfigureRestDocs
@AutoConfigureMockMvc
@SpringBootTest
public abstract class SupportControllerTest {

    // [공통 Mock] 자식 클래스에서는 이것들을 다시 선언하면 안 됩니다! (Duplicate 에러 원인)
    @MockitoBean protected RabbitMqInitializer rabbitMqInitializer;
    @MockitoBean protected CartSyncScheduler cartSyncScheduler;
    @MockitoBean protected RabbitAdmin rabbitAdmin;
    @MockitoBean protected BookSearchRepository bookSearchRepository;
    @MockitoBean protected RabbitTemplate rabbitTemplate;

    // [공통 유틸]
    @Autowired protected ObjectMapper objectMapper;
    @Autowired protected WebApplicationContext webApplicationContext;

    protected MockMvc mockMvc;

    @BeforeEach
    void setUp(RestDocumentationContextProvider restDocumentation) {
        this.mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext)
                .apply(documentationConfiguration(restDocumentation))
                .addFilters(new CharacterEncodingFilter("UTF-8", true))
                .build();
    }
}