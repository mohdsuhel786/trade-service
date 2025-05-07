package com.apexon.next_investor.trade.trade_service.dtos;

import com.apexon.next_investor.trade.trade_service.entities.Trade;
import com.apexon.next_investor.trade.trade_service.enums.OrderType;
import com.apexon.next_investor.trade.trade_service.enums.Status;
import com.apexon.next_investor.trade.trade_service.enums.TimeInForce;
import com.apexon.next_investor.trade.trade_service.enums.TransactionType;
import lombok.*;

@Data
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class TradeDTO {

    private Long clientId;
    private String stockSymbol;
    private OrderType orderType;
    private Integer quantity;
    private Double price;
    private TimeInForce timeInForce;
    private Status status;
    private TransactionType transactionType;

//    private String createdBy;
//    private String modifiedBy;

    // Getters and setters
    // ...


    public TradeDTO(Trade trade) {
      //  this.id = trade.getId();
        this.clientId = trade.getClientId();
        this.stockSymbol = trade.getStockSymbol();
        this.quantity = trade.getQuantity();
        this.price = trade.getPrice();
        this.orderType = trade.getOrderType();
        this.timeInForce = trade.getTimeInForce();
        this.status = trade.getStatus();
        this.transactionType = trade.getTransactionType();
        //this.createdBy = trade.getCreatedBy();
       // this.createdDate = trade.getCreatedDate();
       // this.modifiedBy = trade.getModifiedBy();
       // this.modifiedDate = trade.getModifiedDate();
    }
}
