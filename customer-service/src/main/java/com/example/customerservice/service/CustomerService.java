package com.example.customerservice.service;

import com.example.customerservice.dto.CustomerRequestDTO;
import com.example.customerservice.dto.CustomerResponseDTO;
import com.example.customerservice.dto.LoginRequestDTO;

import java.util.List;

public interface CustomerService {
    CustomerResponseDTO register(CustomerRequestDTO request);
    CustomerResponseDTO getById(Long id);
    List<CustomerResponseDTO> getAll();
    CustomerResponseDTO update(Long id, CustomerRequestDTO request);
    void delete(Long id);
    CustomerResponseDTO login(LoginRequestDTO request);
}
