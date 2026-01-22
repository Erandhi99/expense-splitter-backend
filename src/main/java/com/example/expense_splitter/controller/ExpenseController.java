package com.example.expense_splitter.controller;

import com.example.expense_splitter.dto.ApiResponse;
import com.example.expense_splitter.dto.TransactionSuggestion;
import com.example.expense_splitter.model.Expense;
import com.example.expense_splitter.service.ExpenseService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/expenses")
@RequiredArgsConstructor
public class ExpenseController {

    private final ExpenseService expenseService; // Logic layer

    // 1. Add Expense
    @PostMapping
    public ResponseEntity<ApiResponse<Expense>> addExpense(@RequestBody Expense expense) {
        Expense saved = expenseService.addExpense(expense);
        return ResponseEntity.ok(ApiResponse.success(saved, "Expense added successfully"));
    }

    // 2. Settle Debt
    @PostMapping("/settle")
    public ResponseEntity<ApiResponse<Expense>> settleDebt(@RequestBody Map<String, Object> settlementData) {
        Expense saved = expenseService.settleDebt(settlementData);
        return ResponseEntity.ok(ApiResponse.success(saved, "Settlement recorded successfully"));
    }

    // 3. Get Balances
    @GetMapping("/group/{groupId}/balances")
    public ResponseEntity<ApiResponse<Map<String, Double>>> getBalances(@PathVariable String groupId) {
        Map<String, Double> balances = expenseService.getBalances(groupId);
        return ResponseEntity.ok(ApiResponse.success(balances, "Balances calculated successfully"));
    }

    // 4. Smart Simplification
    @GetMapping("/group/{groupId}/simplify")
    public ResponseEntity<ApiResponse<List<TransactionSuggestion>>> simplifyDebts(@PathVariable String groupId) {
        List<TransactionSuggestion> suggestions = expenseService.simplifyDebts(groupId);
        return ResponseEntity.ok(ApiResponse.success(suggestions, "Simplification calculated successfully"));
    }

    // 5. PDF Report (We can keep this here or move logic later, keeping it here for simplicity now)
    // Note: PDF endpoints return void/stream, so we don't wrap them in ApiResponse
    @GetMapping("/group/{groupId}/report")
    public void generatePdfReport(HttpServletResponse response, @PathVariable String groupId) throws IOException {
        // ... (Keep your existing PDF code here exactly as it was) ...
        // If you need me to paste the PDF code again, let me know!
    }
}