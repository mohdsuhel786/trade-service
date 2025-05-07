package com.apexon.next_investor.trade.trade_service.dtos;

import lombok.*;

@Data
@Getter
@Setter
@NoArgsConstructor

public class AggregatedTradeDTO {
    private String stockSymbol;
    private int totalQuantity;
    private double averagePrice;
    private double totalValue;

    public AggregatedTradeDTO(String stockSymbol, int totalQuantity, double averagePrice, double totalValue) {
        this.stockSymbol = stockSymbol;
        this.totalQuantity = totalQuantity;
        this.averagePrice = averagePrice;
        this.totalValue = totalValue;
    }

    // Getters and setters
}

