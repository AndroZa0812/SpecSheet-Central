package com.specsheetcentral.service;

import com.specsheetcentral.dto.OrderRequest;
import com.specsheetcentral.dto.OrderResponse;
import com.specsheetcentral.model.*;
import com.specsheetcentral.repository.OrderRepository;
import com.specsheetcentral.repository.ProductRepository;
import com.specsheetcentral.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrderServiceTest {

    @Mock
    private OrderRepository orderRepository;
    @Mock
    private ProductRepository productRepository;
    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private OrderService orderService;

    private User user;
    private Product product;
    private Order order;

    @BeforeEach
    void setUp() {
        user = new User();
        user.setId(1L);
        user.setEmail("user@example.com");
        user.setRole(User.Role.USER);

        product = new Product();
        product.setId(10L);
        product.setName("Sensor");
        product.setPrice(25.0);
        product.setStockQuantity(100);

        order = new Order();
        order.setId(1L);
        order.setUser(user);
        order.setOrderDate(java.time.LocalDateTime.now());
        order.setTotalAmount(50.0);
        order.setStatus(Order.Status.PENDING);

        OrderItem item = new OrderItem();
        item.setOrder(order);
        item.setProduct(product);
        item.setQuantity(2);
        item.setPriceAtPurchase(25.0);
        order.getItems().add(item);
    }

    @Test
    void findByUser_returnsOrdersForUser() {
        when(orderRepository.findByUserEmail("user@example.com")).thenReturn(List.of(order));

        List<OrderResponse> results = orderService.findByUser("user@example.com");

        assertThat(results).hasSize(1);
        assertThat(results.get(0).getUserEmail()).isEqualTo("user@example.com");
        assertThat(results.get(0).getTotalAmount()).isEqualTo(50.0);
        verify(orderRepository).findByUserEmail("user@example.com");
    }

    @Test
    void findAll_returnsAllOrders() {
        when(orderRepository.findAll()).thenReturn(List.of(order));

        List<OrderResponse> results = orderService.findAll();

        assertThat(results).hasSize(1);
        assertThat(results.get(0).getStatus()).isEqualTo("PENDING");
        verify(orderRepository).findAll();
    }

    @Test
    void create_insufficientStock_throwsException() {
        OrderRequest.OrderItemRequest itemReq = new OrderRequest.OrderItemRequest();
        itemReq.setProductId(10L);
        itemReq.setQuantity(200);

        OrderRequest request = new OrderRequest();
        request.setItems(List.of(itemReq));

        when(userRepository.findByEmail("user@example.com")).thenReturn(Optional.of(user));
        when(productRepository.findById(10L)).thenReturn(Optional.of(product));

        assertThatThrownBy(() -> orderService.create("user@example.com", request))
            .isInstanceOf(IllegalStateException.class)
            .hasMessage("Insufficient stock for Sensor");
    }

    @Test
    void updateStatus_validStatus_updatesOrder() {
        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));
        when(orderRepository.save(any(Order.class))).thenAnswer(invocation -> {
            Order o = invocation.getArgument(0);
            o.setId(1L);
            return o;
        });

        OrderResponse result = orderService.updateStatus(1L, "DELIVERED");

        assertThat(result.getStatus()).isEqualTo("DELIVERED");
        verify(orderRepository).save(any(Order.class));
    }

    @Test
    void create_nonexistentUser_throwsException() {
        OrderRequest.OrderItemRequest itemReq = new OrderRequest.OrderItemRequest();
        itemReq.setProductId(10L);
        itemReq.setQuantity(1);

        OrderRequest request = new OrderRequest();
        request.setItems(List.of(itemReq));

        when(userRepository.findByEmail("nobody@example.com")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> orderService.create("nobody@example.com", request))
            .isInstanceOf(EntityNotFoundException.class)
            .hasMessage("User not found");
    }
}