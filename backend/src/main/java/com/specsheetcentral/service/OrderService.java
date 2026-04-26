package com.specsheetcentral.service;

import com.specsheetcentral.dto.OrderRequest;
import com.specsheetcentral.dto.OrderResponse;
import com.specsheetcentral.model.Order;
import com.specsheetcentral.model.OrderItem;
import com.specsheetcentral.model.Product;
import com.specsheetcentral.model.User;
import com.specsheetcentral.repository.OrderRepository;
import com.specsheetcentral.repository.ProductRepository;
import com.specsheetcentral.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class OrderService {
    private final OrderRepository orderRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;

    public OrderService(OrderRepository orderRepository,
                        ProductRepository productRepository,
                        UserRepository userRepository) {
        this.orderRepository = orderRepository;
        this.productRepository = productRepository;
        this.userRepository = userRepository;
    }

    @Transactional
    public OrderResponse create(String userEmail, OrderRequest request) {
        User user = userRepository.findByEmail(userEmail)
            .orElseThrow(() -> new EntityNotFoundException("User not found"));

        Order order = new Order();
        order.setUser(user);
        order.setOrderDate(LocalDateTime.now());
        order.setStatus(Order.Status.PENDING);

        double total = 0;
        for (OrderRequest.OrderItemRequest itemReq : request.getItems()) {
            Product product = productRepository.findById(itemReq.getProductId())
                .orElseThrow(() -> new EntityNotFoundException("Product not found"));
            if (product.getStockQuantity() < itemReq.getQuantity()) {
                throw new IllegalStateException("Insufficient stock for " + product.getName());
            }
            product.setStockQuantity(product.getStockQuantity() - itemReq.getQuantity());
            productRepository.save(product);

            OrderItem item = new OrderItem();
            item.setOrder(order);
            item.setProduct(product);
            item.setQuantity(itemReq.getQuantity());
            item.setPriceAtPurchase(product.getPrice());
            order.getItems().add(item);
            total += product.getPrice() * itemReq.getQuantity();
        }

        order.setTotalAmount(total);
        Order saved = orderRepository.save(order);
        return toResponse(saved);
    }

    public List<OrderResponse> findByUser(String userEmail) {
        return orderRepository.findByUserEmailOrderByOrderDateDesc(userEmail).stream()
            .map(this::toResponse)
            .toList();
    }

    public List<OrderResponse> findAll() {
        return orderRepository.findAll(org.springframework.data.domain.Sort.by(org.springframework.data.domain.Sort.Order.desc("id"))).stream()
            .map(this::toResponse)
            .toList();
    }

    @Transactional
    public OrderResponse updateStatus(Long id, String status) {
        Order order = orderRepository.findById(id)
            .orElseThrow(() -> new EntityNotFoundException("Order not found"));

        Order.Status newStatus = Order.Status.valueOf(status);
        Order.Status oldStatus = order.getStatus();

        if (newStatus == Order.Status.CANCELLED && oldStatus != Order.Status.CANCELLED) {
            for (OrderItem item : order.getItems()) {
                Product product = item.getProduct();
                product.setStockQuantity(product.getStockQuantity() + item.getQuantity());
                productRepository.save(product);
            }
        } else if (oldStatus == Order.Status.CANCELLED && newStatus != Order.Status.CANCELLED) {
            for (OrderItem item : order.getItems()) {
                Product product = item.getProduct();
                if (product.getStockQuantity() < item.getQuantity()) {
                    throw new IllegalStateException("Insufficient stock for " + product.getName());
                }
                product.setStockQuantity(product.getStockQuantity() - item.getQuantity());
                productRepository.save(product);
            }
        }

        order.setStatus(newStatus);
        return toResponse(orderRepository.save(order));
    }

    private OrderResponse toResponse(Order order) {
        OrderResponse response = new OrderResponse();
        response.setId(order.getId());
        response.setUserEmail(order.getUser().getEmail());
        response.setOrderDate(order.getOrderDate());
        response.setTotalAmount(order.getTotalAmount());
        response.setStatus(order.getStatus().name());
        response.setItems(order.getItems().stream().map(item -> {
            OrderResponse.OrderItemResponse i = new OrderResponse.OrderItemResponse();
            i.setProductId(item.getProduct().getId());
            i.setProductName(item.getProduct().getName());
            i.setQuantity(item.getQuantity());
            i.setPriceAtPurchase(item.getPriceAtPurchase());
            return i;
        }).toList());
        return response;
    }
}
