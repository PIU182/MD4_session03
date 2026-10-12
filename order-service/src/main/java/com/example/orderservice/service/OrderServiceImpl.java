package com.example.orderservice.service;

import com.example.orderservice.client.ProductServiceClient;
import com.example.orderservice.dto.OrderRequestDTO;
import com.example.orderservice.dto.OrderResponseDTO;
import com.example.orderservice.dto.ProductClientResponseDTO;
import com.example.orderservice.entity.Order;
import com.example.orderservice.exception.BadRequestException;
import com.example.orderservice.exception.DatabaseOperationException;
import com.example.orderservice.exception.ResourceNotFoundException;
import com.example.orderservice.repository.OrderRepository;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Service
public class OrderServiceImpl implements OrderService {

    private final OrderRepository orderRepository;
    private final ProductServiceClient productServiceClient;

    public OrderServiceImpl(OrderRepository orderRepository,
                            ProductServiceClient productServiceClient) {
        this.orderRepository = orderRepository;
        this.productServiceClient = productServiceClient;
    }

    @Override
    public OrderResponseDTO create(OrderRequestDTO request) {
        if (request.getQuantity() == null || request.getQuantity() <= 0) {
            throw new BadRequestException("Quantity must be greater than 0");
        }

        ProductClientResponseDTO product =
                productServiceClient.getProductById(request.getProductId());

        if (product.getStockQuantity() != null
                && request.getQuantity() > product.getStockQuantity()) {
            throw new BadRequestException("Requested quantity exceeds product stock");
        }

        BigDecimal totalAmount = product.getPrice()
                .multiply(BigDecimal.valueOf(request.getQuantity()));

        Order order = new Order();
        order.setCustomerId(request.getCustomerId());
        order.setProductId(request.getProductId());
        order.setOrderDate(LocalDateTime.now());
        order.setTotalAmount(totalAmount);
        order.setStatus("CREATED");

        try {
            return toResponse(orderRepository.save(order));
        } catch (DataAccessException ex) {
            throw new DatabaseOperationException(
                    "Failed to save order to database",
                    ex
            );
        }
    }

    @Override
    public OrderResponseDTO getById(Long id) {
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Order not found with id = " + id
                ));
        return toResponse(order);
    }

    private OrderResponseDTO toResponse(Order order) {
        return new OrderResponseDTO(
                order.getId(),
                order.getCustomerId(),
                order.getProductId(),
                order.getOrderDate(),
                order.getTotalAmount(),
                order.getStatus()
        );
    }
}
