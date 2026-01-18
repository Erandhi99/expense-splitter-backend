package com.example.expense_splitter.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class TransactionSuggestion {
    private String payerId;
    private String receiverId;
    private double amount;
}