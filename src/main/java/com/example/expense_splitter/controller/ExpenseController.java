package com.example.expense_splitter.controller;

import com.example.expense_splitter.model.Expense;
import com.example.expense_splitter.repository.ExpenseRepository;
import com.example.expense_splitter.service.ExpenseService;
import lombok.RequiredArgsConstructor;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.List;

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

    @PostMapping("/settle")
    public ResponseEntity<Expense> settleDebt(@RequestBody Map<String, Object> settlementData) {
        /*
          Expected JSON:
          {
            "groupId": "group1",
            "payerId": "BOB_ID",
            "receiverId": "ALICE_ID",
            "amount": 50.0
          }
        */
        
        String groupId = (String) settlementData.get("groupId");
        String payerId = (String) settlementData.get("payerId");
        String receiverId = (String) settlementData.get("receiverId");
        
        // Handle double/integer conversion safely
        Double amount = 0.0;
        if (settlementData.get("amount") instanceof Integer) {
            amount = ((Integer) settlementData.get("amount")).doubleValue();
        } else {
            amount = (Double) settlementData.get("amount");
        }

        // Create the Expense object logic
        Expense settlement = new Expense();
        settlement.setGroupId(groupId);
        settlement.setAmount(amount);
        settlement.setPaidBy(payerId);
        settlement.setDescription("Settlement Payment");
        
        // The key to settlement: The receiver "consumes" the full amount
        settlement.setSplitAmong(List.of(receiverId));

        Expense saved = expenseRepo.save(settlement);
        return ResponseEntity.ok(saved);
    }
}