package com.apexon.next_investor.trade.trade_service.repositories;


import com.apexon.next_investor.trade.trade_service.entities.Trade;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TradeRepository extends JpaRepository<Trade, Long> {
    List<Trade> findByClientId(Long clientId);
    List<Trade> findByClientIdAndStockSymbol(Long clientId, String stockSymbol);
}

