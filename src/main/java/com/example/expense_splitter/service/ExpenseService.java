package com.example.expense_splitter.service;

import com.example.expense_splitter.dto.TransactionSuggestion;
import com.example.expense_splitter.model.Expense;
import com.example.expense_splitter.repository.ExpenseRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
@RequiredArgsConstructor // Automatically injects the Repository
public class ExpenseService {

    private final ExpenseRepository expenseRepo;

    // 1. Add Expense Logic
    public Expense addExpense(Expense expense) {
        return expenseRepo.save(expense);
    }

    // 2. Settle Debt Logic
    public Expense settleDebt(Map<String, Object> settlementData) {
        String groupId = (String) settlementData.get("groupId");
        String payerId = (String) settlementData.get("payerId");
        String receiverId = (String) settlementData.get("receiverId");

        // Safe conversion for amount
        Double amount;
        if (settlementData.get("amount") instanceof Integer) {
            amount = ((Integer) settlementData.get("amount")).doubleValue();
        } else {
            amount = (Double) settlementData.get("amount");
        }

        Expense settlement = new Expense();
        settlement.setGroupId(groupId);
        settlement.setAmount(amount);
        settlement.setPaidBy(payerId);
        settlement.setDescription("Settlement Payment");
        settlement.setSplitAmong(List.of(receiverId)); // Receiver consumes 100%

        return expenseRepo.save(settlement);
    }

    // 3. Get Balances Logic (Updated to fetch from DB internally)
    public Map<String, Double> getBalances(String groupId) {
        List<Expense> expenses = expenseRepo.findByGroupId(groupId);
        return calculateBalancesInternal(expenses);
    }

    // 4. Smart Simplification Logic
    public List<TransactionSuggestion> simplifyDebts(String groupId) {
        List<Expense> expenses = expenseRepo.findByGroupId(groupId);
        Map<String, Double> balances = calculateBalancesInternal(expenses);

        // Separate Debtors and Creditors
        List<String> debtors = new ArrayList<>();
        List<String> creditors = new ArrayList<>();

        for (Map.Entry<String, Double> entry : balances.entrySet()) {
            if (entry.getValue() < -0.01) debtors.add(entry.getKey());
            else if (entry.getValue() > 0.01) creditors.add(entry.getKey());
        }

        List<TransactionSuggestion> suggestions = new ArrayList<>();

        // Greedy Algorithm
        while (!debtors.isEmpty() && !creditors.isEmpty()) {
            // Sort to match biggest debtor with biggest creditor
            debtors.sort(Comparator.comparingDouble(balances::get)); // Ascending (Most negative first)
            creditors.sort((a, b) -> Double.compare(balances.get(b), balances.get(a))); // Descending (Max positive first)

            String debtor = debtors.get(0);
            String creditor = creditors.get(0);

            double debtAmount = Math.abs(balances.get(debtor));
            double creditAmount = balances.get(creditor);
            double settleAmount = Math.min(debtAmount, creditAmount);

            // Rounding
            settleAmount = Math.round(settleAmount * 100.0) / 100.0;

            if (settleAmount > 0) {
                suggestions.add(new TransactionSuggestion(debtor, creditor, settleAmount));
            }

            // Update remaining balances
            balances.put(debtor, balances.get(debtor) + settleAmount);
            balances.put(creditor, balances.get(creditor) - settleAmount);

            if (Math.abs(balances.get(debtor)) < 0.01) debtors.remove(debtor);
            if (balances.get(creditor) < 0.01) creditors.remove(creditor);
        }

        return suggestions;
    }

    // --- Private Helper Method (Your original logic) ---
    private Map<String, Double> calculateBalancesInternal(List<Expense> expenses) {
        Map<String, Double> balances = new HashMap<>();
        for (Expense e : expenses) {
            double splitAmount = e.getAmount() / e.getSplitAmong().size();
            for (String userId : e.getSplitAmong()) {
                balances.put(userId, balances.getOrDefault(userId, 0.0) - splitAmount);
            }
            balances.put(e.getPaidBy(), balances.getOrDefault(e.getPaidBy(), 0.0) + e.getAmount());
        }
        return balances;
    }
}