package com.mikael.paymybuddy.DTO;

import java.math.BigDecimal;

public class TransactionRequestDTO {
    private Long senderId;
    private Long receiverId;
    private BigDecimal amount;
    private String description;

    public TransactionRequestDTO() {}

    public TransactionRequestDTO(Long senderId, Long receiverId, BigDecimal amount, String description) {
        this.senderId = senderId;
        this.receiverId = receiverId;
        this.amount = amount;
        this.description = description;
    }

    public Long getReceiverId() {
        return receiverId;
    }

    public void setReceiverId(Long receiverId) {
        this.receiverId = receiverId;
    }

    public Long getSenderId() {
        return senderId;
    }

    public void setSenderId(Long senderId) {
        this.senderId = senderId;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}