package com.nhnacademy._vidiabookstoreservice.order.service.impl;

import com.nhnacademy._vidiabookstoreservice.book.domain.Book;
import com.nhnacademy._vidiabookstoreservice.book.service.BookService;
import com.nhnacademy._vidiabookstoreservice.order.domain.Order;
import com.nhnacademy._vidiabookstoreservice.order.domain.OrderItem;
import com.nhnacademy._vidiabookstoreservice.order.domain.Packaging;
import com.nhnacademy._vidiabookstoreservice.order.domain.PackagingOption;
import com.nhnacademy._vidiabookstoreservice.order.domain.enums.ConfirmStatus;
import com.nhnacademy._vidiabookstoreservice.order.domain.enums.OrderStatus;
import com.nhnacademy._vidiabookstoreservice.order.dto.order.request.OrderCreateRequest;
import com.nhnacademy._vidiabookstoreservice.order.dto.order.response.DeliveryDateResponse;
import com.nhnacademy._vidiabookstoreservice.order.dto.order.response.OrderResponse;
import com.nhnacademy._vidiabookstoreservice.order.exception.OrderNotFoundException;
import com.nhnacademy._vidiabookstoreservice.order.repository.OrderItemRepository;
import com.nhnacademy._vidiabookstoreservice.order.repository.OrderRepository;
import com.nhnacademy._vidiabookstoreservice.order.repository.PackagingOptionRepository;
import com.nhnacademy._vidiabookstoreservice.order.repository.PackagingRepository;
import com.nhnacademy._vidiabookstoreservice.order.service.OrderService;
import com.nhnacademy._vidiabookstoreservice.user.domain.User;
import com.nhnacademy._vidiabookstoreservice.user.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.NoSuchElementException;

@Service
@RequiredArgsConstructor
@Transactional
public class OrderServiceImpl implements OrderService {

    private final UserService userService;
    private final BookService bookService;
    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final PackagingRepository packagingRepository;
    private final PackagingOptionRepository packagingOptionRepository;

    @Override
    public List<DeliveryDateResponse> getDeliveryDates() {
        LocalDate date = LocalDate.now();

        List<DeliveryDateResponse> deliveryDateResponseList = new ArrayList<>();

        for (int i=2; i<=7; i++) {
            deliveryDateResponseList.add(new DeliveryDateResponse(
                    date.plusDays(i).format(DateTimeFormatter.ofPattern("yyyy-MM-dd")),
                    date.plusDays(i).format(DateTimeFormatter.ofPattern("yyyy-MM-dd (E)", Locale.KOREAN))
            ));
        }
        return deliveryDateResponseList;
    }

    @Override
    public Long saveOrder(Long userId, OrderCreateRequest request) {
        User user = userService.getUserById(userId);

        //TODO 재고 차감 구현 wow 어떻게하냐

        Order order = Order.builder()
                .user(user)
                .recipientName(request.recipientName())
                .addressRoadname(request.addressRoadname())
                .addressDetail(request.addressDetail())
                .zipCode(request.zipCode())
                .recipientPhone(request.recipientPhone())
                .deliveryRequest(request.deliveryRequest())
                .couponDiscount(request.couponDiscount())
                .pointUsed(request.pointUsed())
                .deliveryDate(request.deliveryDate())
                .totalPrice(request.totalPrice() + request.deliveryCost() + request.packagingCost())
                .payPrice(request.payPrice())
                .build();

        Order savedOrder = orderRepository.save(order);

        for (OrderCreateRequest.ItemRequestDto itemDto : request.orderItems()) {
            Book book = bookService.getBookEntity(itemDto.bookId());

            OrderItem orderItem = OrderItem.builder()
                    .order(savedOrder)
                    .book(book)
                    .quantity(itemDto.quantity())
                    .salePrice(itemDto.salePrice())
                    .confirmStatus(ConfirmStatus.UNCONFIRMED)
                    .build();

            savedOrder.getOrderItems().add(orderItem);

            OrderItem savedOrderItem = orderItemRepository.save(orderItem);

            for (Long packagingOptionId : itemDto.packagingOptionIds()) {
                if (packagingOptionId != 0) {
                    PackagingOption packagingOption = packagingOptionRepository.findById(packagingOptionId)
                            .orElseThrow(() -> new NoSuchElementException("Packaging Option not found: " + packagingOptionId));


                    Packaging packaging = Packaging.builder()
                            .orderItem(savedOrderItem)
                            .packagingOption(packagingOption)
                            .build();

                    packagingRepository.save(packaging);
                }
            }


        }

        return savedOrder.getOrderId();
    }

    @Override
    @Transactional(readOnly = true)
    public OrderResponse getOrderResponse(Long orderId) {
        Order order = orderRepository.findByOrderId(orderId).orElseThrow(
                () -> new OrderNotFoundException("ID에 해당하는 주문내역을 찾을 수 없습니다. ID: %d".formatted(orderId))
        );

        return OrderResponse.from(order);
    }

    @Override
    @Transactional(readOnly = true)
    public Order getOrder(Long orderId) {
        Order order = orderRepository.findByOrderId(orderId).orElseThrow(
                () -> new OrderNotFoundException("ID에 해당하는 주문내역을 찾을 수 없습니다. ID: %d".formatted(orderId))
        );

        return order;
    }

    @Override
    public void updateOrderStatus(Long orderId, OrderStatus orderStatus) {
        Order order = orderRepository.findByOrderId(orderId).orElseThrow(
                () -> new OrderNotFoundException("ID에 해당하는 주문내역을 찾을 수 없습니다. ID: %d".formatted(orderId))
        );

        order.setOrderStatus(orderStatus);
    }

    @Override
    @Transactional(readOnly = true)
    public List<OrderResponse> getOrdersByUserId(Long userId) {
        List<Order> orders = orderRepository.findAllByUser_UserId(userId);

        if (orders.isEmpty()) {
            throw new OrderNotFoundException("userId에 해당하는 주문내역을 찾을 수 없습니다. ID: %d".formatted(userId));
        }

        return orders.stream().map(OrderResponse::from).toList();
    }
}
