package com.example.expense_splitter.service;

import com.example.expense_splitter.model.Expense;
import org.springframework.stereotype.Service;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class ExpenseService {

    public Map<String, Double> calculateBalances(List<Expense> expenses) {
        Map<String, Double> balances = new HashMap<>();

        for (Expense e : expenses) {
            // How much each person (including payer) owes for this expense
            double splitAmount = e.getAmount() / e.getSplitAmong().size();

            // 1. Deduct splitAmount from everyone involved (they "owe" this money)
            for (String userId : e.getSplitAmong()) {
                balances.put(userId, balances.getOrDefault(userId, 0.0) - splitAmount);
            }

            // 2. Add full amount to the payer (they are "owed" this money)
            // Note: The payer was also deducted above, so their net change is:
            // (Paid - SplitAmount) = Positive Balance (Owed to them)
            balances.put(e.getPaidBy(), balances.getOrDefault(e.getPaidBy(), 0.0) + e.getAmount());
        }
        return balances;
    }
}
