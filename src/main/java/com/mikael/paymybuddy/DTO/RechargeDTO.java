package com.mikael.paymybuddy.DTO;

import java.math.BigDecimal;

public class RechargeDTO {

    private BigDecimal amount;

    public RechargeDTO() {
    }

    public RechargeDTO(BigDecimal amount) {
        this.amount = amount;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }
}
