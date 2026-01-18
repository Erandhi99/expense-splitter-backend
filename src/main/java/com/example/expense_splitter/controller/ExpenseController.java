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

    @GetMapping("/group/{groupId}/simplify")
    public List<TransactionSuggestion> simplifyDebts(@PathVariable String groupId) {
        
        // --- STEP 1: Calculate Net Balances (Same logic as your balances endpoint) ---
        List<Expense> expenses = expenseRepo.findByGroupId(groupId);
        Map<String, Double> balances = new HashMap<>();

        for (Expense expense : expenses) {
            double splitAmount = expense.getAmount() / expense.getSplitAmong().size();
            
            // Payer gets positive balance (They are owed money)
            balances.put(expense.getPaidBy(), 
                balances.getOrDefault(expense.getPaidBy(), 0.0) + expense.getAmount());

            // Consumers get negative balance (They owe money)
            for (String userId : expense.getSplitAmong()) {
                balances.put(userId, 
                    balances.getOrDefault(userId, 0.0) - splitAmount);
            }
        }

        // --- STEP 2: Separate Debtors and Creditors ---
        // We filter out people with roughly 0 balance
        List<String> debtors = new ArrayList<>();
        List<String> creditors = new ArrayList<>();

        for (String user : balances.keySet()) {
            double amount = balances.get(user);
            // Use a small threshold for floating point errors
            if (amount < -0.01) debtors.add(user);
            else if (amount > 0.01) creditors.add(user);
        }

        // --- STEP 3: Greedy Algorithm Loop ---
        List<TransactionSuggestion> suggestions = new ArrayList<>();

        // While we still have people who owe and people who are owed
        while (!debtors.isEmpty() && !creditors.isEmpty()) {
            
            // Sort to find the BIGGEST debtor and BIGGEST creditor
            // (Greedy approach: match max negative with max positive)
            debtors.sort(Comparator.comparingDouble(balances::get)); // Ascending (Most negative first)
            creditors.sort((a, b) -> Double.compare(balances.get(b), balances.get(a))); // Descending (Most positive first)

            String debtor = debtors.get(0);
            String creditor = creditors.get(0);

            double debtAmount = Math.abs(balances.get(debtor));
            double creditAmount = balances.get(creditor);

            // The amount to settle is the minimum of the two
            // Example: If Bob owes 50 but Alice is only owed 30, Bob pays Alice 30.
            double settleAmount = Math.min(debtAmount, creditAmount);
            
            // Round to 2 decimal places for clean display
            settleAmount = Math.round(settleAmount * 100.0) / 100.0;

            if (settleAmount > 0) {
                suggestions.add(new TransactionSuggestion(debtor, creditor, settleAmount));
            }

            // Update balances after this "virtual" payment
            balances.put(debtor, balances.get(debtor) + settleAmount);
            balances.put(creditor, balances.get(creditor) - settleAmount);

            // Remove settled users from the lists
            if (Math.abs(balances.get(debtor)) < 0.01) debtors.remove(debtor);
            if (balances.get(creditor) < 0.01) creditors.remove(creditor);
        }

        return suggestions;
    }
}