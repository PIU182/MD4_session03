package com.example.orderservice.client;

import com.example.orderservice.dto.ProductClientResponseDTO;
import com.example.orderservice.exception.BadRequestException;
import com.example.orderservice.exception.ExternalServiceException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
public class ProductServiceClient {

    private final RestClient restClient;

    public ProductServiceClient(@Value("${product.service.base-url}") String baseUrl) {
        this.restClient = RestClient.builder()
                .baseUrl(baseUrl)
                .build();
    }

    public ProductClientResponseDTO getProductById(Long productId) {
        try {
            ProductClientResponseDTO product = restClient.get()
                    .uri("/api/v1/products/{id}", productId)
                    .retrieve()
                    .onStatus(HttpStatusCode::is4xxClientError, (request, response) -> {
                        throw new BadRequestException(
                                "Product does not exist with id = " + productId
                        );
                    })
                    .onStatus(HttpStatusCode::is5xxServerError, (request, response) -> {
                        throw new ExternalServiceException(
                                "Product Service is unavailable"
                        );
                    })
                    .body(ProductClientResponseDTO.class);

            if (product == null || product.getPrice() == null) {
                throw new ExternalServiceException("Invalid response from Product Service");
            }

            return product;
        } catch (BadRequestException | ExternalServiceException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new ExternalServiceException(
                    "Cannot connect to Product Service at port 8082"
            );
        }
    }
}
