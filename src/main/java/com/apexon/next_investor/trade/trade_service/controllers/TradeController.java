package com.apexon.next_investor.trade.trade_service.controllers;



import com.apexon.next_investor.trade.trade_service.dtos.AggregatedTradeDTO;
import com.apexon.next_investor.trade.trade_service.dtos.TradeDTO;
import com.apexon.next_investor.trade.trade_service.services.TradeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/trades")
public class TradeController {

    @Autowired
    private TradeService tradeService;

    @PostMapping
    public TradeDTO createTrade(@RequestBody TradeDTO tradeDTO) {
        return tradeService.createTrade(tradeDTO);
    }

    @GetMapping("/client/{clientId}")
    public Map<String, Object> getTradesByClientId(@PathVariable Long clientId) {
        return tradeService.getTradeAndStockDetails(clientId);
    }

    @DeleteMapping("/{id}")
    public void deleteTrade(@PathVariable Long id) {
        tradeService.deleteTrade(id);
    }

    @GetMapping("/filter")
    public ResponseEntity<Map<String, Object>> getFilteredTrades(
            @RequestParam("clientId") Long clientId,
            @RequestParam("stockSymbol") String stockSymbol
    ) {
        Map<String, Object> result = tradeService.getTradeAndStockDetails(clientId, stockSymbol);

        if (result.isEmpty()) {
            return ResponseEntity.noContent().build();  // No data found for the given clientId and symbol
        }

        return ResponseEntity.ok(result);
    }

//    @PostMapping("/execute")
//    public ResponseEntity<String> executeTrade(@RequestBody TradeDTO tradeDTO) {
//        String result = tradeService.executeTrade(tradeDTO);
//        return ResponseEntity.ok(result);
//    }
}
