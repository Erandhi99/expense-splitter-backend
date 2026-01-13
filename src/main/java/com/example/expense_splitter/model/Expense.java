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
    private LocalDateTime createdAt;

    public List<String> getSplitAmong() {
        return splitBetween;
    }

    public void setSplitAmong(List<String> splitBetween) {
        this.splitBetween = splitBetween;
    }

    public double getAmount() {
        return amount;
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }

    public String getPaidBy() {
        return paidBy;
    }

    public void setPaidBy(String paidBy) {
        this.paidBy = paidBy;
    }
    
    // Add other getters/setters if you see more errors (getId, setCreatedAt, etc.)
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    
    public String getGroupId() { return groupId; }
    public void setGroupId(String groupId) { this.groupId = groupId; }
    
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
