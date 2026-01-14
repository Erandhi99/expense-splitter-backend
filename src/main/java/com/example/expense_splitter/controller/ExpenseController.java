package com.example.expense_splitter.controller;

import com.example.expense_splitter.model.Expense;
import com.example.expense_splitter.repository.ExpenseRepository;
import com.example.expense_splitter.service.ExpenseService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDateTime;
import java.util.Map;

@RestController
@RequestMapping("/api/expenses")
@RequiredArgsConstructor
public class ExpenseController {

    private final ExpenseRepository expenseRepo;
    private final ExpenseService expenseService;

    @PostMapping
    public Expense add(@RequestBody Expense expense) {
        expense.setCreatedAt(LocalDateTime.now());
        return expenseRepo.save(expense);
    }

    @GetMapping("/group/{groupId}/balances")
    public Map<String, Double> getBalances(@PathVariable String groupId) {
        return expenseService.calculateBalances(
                expenseRepo.findByGroupId(groupId)
        );
    }
}