package com.apexon.next_investor.trade.trade_service.entities;



import com.apexon.next_investor.trade.trade_service.enums.OrderType;
import com.apexon.next_investor.trade.trade_service.enums.Status;
import com.apexon.next_investor.trade.trade_service.enums.TimeInForce;
import com.apexon.next_investor.trade.trade_service.enums.TransactionType;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;


import java.time.LocalDateTime;

@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "trades")
@Data
public class Trade extends Audit {

    // Getters and setters
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long clientId;

    @Column(length = 10)
    private String stockSymbol;

    @Enumerated(EnumType.STRING)
    private OrderType orderType;

    private Integer quantity;

    private Double price;

    @Enumerated(EnumType.STRING)
    private TimeInForce timeInForce;

    @Enumerated(EnumType.STRING)
    private Status status;

    @Enumerated(EnumType.STRING)
    private TransactionType transactionType;

//    @Column(nullable = false, updatable = false)
    private LocalDateTime tradeBuyTime;
//    @Column(nullable = false, updatable = false)
    private LocalDateTime tradeSellTime;
//    @Column(nullable = false, updatable = false)
    private LocalDateTime tradeCancelTime;

    private LocalDateTime tradeExecutedTime;

}
