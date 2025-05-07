package com.apexon.next_investor.trade.trade_service.Exceptions;

public class TradeNotFoundException extends Exception{
    public TradeNotFoundException() {
    }

    public TradeNotFoundException(String message) {
        super(message);
    }

    public TradeNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
}
