package com.specsheetcentral.controller;

import com.specsheetcentral.dto.OrderRequest;
import com.specsheetcentral.dto.OrderResponse;
import com.specsheetcentral.service.OrderService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/orders")
public class OrderController {
    private final OrderService orderService;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    @PostMapping
    public OrderResponse create(@AuthenticationPrincipal UserDetails user,
                                 @RequestBody OrderRequest request) {
        return orderService.create(user.getUsername(), request);
    }

    @GetMapping("/my")
    public List<OrderResponse> getMyOrders(@AuthenticationPrincipal UserDetails user) {
        return orderService.findByUser(user.getUsername());
    }

    @GetMapping
    public List<OrderResponse> getAll() {
        return orderService.findAll();
    }

    @PatchMapping("/{id}/status")
    public OrderResponse updateStatus(@PathVariable Long id, @RequestParam String status) {
        return orderService.updateStatus(id, status);
    }
}