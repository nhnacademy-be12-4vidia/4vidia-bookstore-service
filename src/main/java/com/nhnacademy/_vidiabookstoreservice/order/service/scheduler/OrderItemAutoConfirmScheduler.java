package com.nhnacademy._vidiabookstoreservice.order.service.scheduler;


import com.nhnacademy._vidiabookstoreservice.order.service.OrderItemService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class OrderItemAutoConfirmScheduler {

    private final OrderItemService orderItemService;

    @Scheduled(cron = "0 10 3 * * *", zone = "Asia/Seoul")
//@Scheduled(cron = "*/10 * * * * *", zone = "Asia/Seoul")

    public void run(){
        int updated = orderItemService.autoConfirmDeliveredOrderItems();
        log.info("OrderItemAutoConfirmScheduler run updated = {}", updated);
    }
}
