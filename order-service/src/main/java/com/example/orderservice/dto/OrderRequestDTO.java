package com.example.orderservice.dto;

import jakarta.validation.constraints.NotNull;

public class OrderRequestDTO {

    @NotNull(message = "Customer id must not be null")
    private Long customerId;

    @NotNull(message = "Product id must not be null")
    private Long productId;

    @NotNull(message = "Quantity must not be null")
    private Integer quantity;

    public Long getCustomerId() { return customerId; }
    public void setCustomerId(Long customerId) { this.customerId = customerId; }

    public Long getProductId() { return productId; }
    public void setProductId(Long productId) { this.productId = productId; }

    public Integer getQuantity() { return quantity; }
    public void setQuantity(Integer quantity) { this.quantity = quantity; }
}
