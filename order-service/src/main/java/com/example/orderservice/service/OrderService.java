package com.example.orderservice.service;

import com.example.orderservice.dto.OrderRequestDTO;
import com.example.orderservice.dto.OrderResponseDTO;

public interface OrderService {
    OrderResponseDTO create(OrderRequestDTO request);
    OrderResponseDTO getById(Long id);
}
