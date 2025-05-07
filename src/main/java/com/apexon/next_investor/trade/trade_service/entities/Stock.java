package com.apexon.next_investor.trade.trade_service.entities;


import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;


@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "stocks")
@Data
public class Stock extends Audit{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String symbol;
    private Integer availableQuantity;
    private Double price;

    // Getters and Setters
}
