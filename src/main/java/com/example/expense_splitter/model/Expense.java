package com.example.expense_splitter.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;

@Document(collection = "expenses")
@Data
public class Expense {
    @Id
    private String id;
    private String groupId;
    private String description;
    private double amount;
    private String paidBy;
    private List<String> splitBetween;
    private LocalDateTime date;
}
