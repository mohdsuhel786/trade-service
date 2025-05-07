package com.apexon.next_investor.trade.trade_service.repositories;

import com.apexon.next_investor.trade.trade_service.entities.Stock;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface StockRepository extends JpaRepository<Stock, Long> {
    Stock findBySymbol(String symbol);
}

