package com.apexon.next_investor.trade.trade_service.services;

import com.apexon.next_investor.trade.trade_service.dtos.TradeDTO;
import com.apexon.next_investor.trade.trade_service.entities.Stock;
import com.apexon.next_investor.trade.trade_service.entities.Trade;
import com.apexon.next_investor.trade.trade_service.enums.TransactionType;
import com.apexon.next_investor.trade.trade_service.repositories.TradeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Transactional
public class TradeService {

    @Autowired
    private TradeRepository tradeRepository;

//    @Autowired
//    private StockServiceClient stockServiceClient; // External Stock Service Client


    public TradeDTO createTrade(TradeDTO tradeDTO) {
//        if (tradeDTO.getQuantity() <= 0) {
//            return "Invalid quantity";
//        }
//
//        if (tradeDTO.getPrice() <= 0) {
//            return "Invalid price";
//        }
        Trade trade = new Trade();
        trade.setClientId(tradeDTO.getClientId());
        trade.setStockSymbol(tradeDTO.getStockSymbol());
        trade.setOrderType(tradeDTO.getOrderType());
        trade.setQuantity(tradeDTO.getQuantity());
        trade.setPrice(tradeDTO.getPrice());
        trade.setTimeInForce(tradeDTO.getTimeInForce());
        // condition change Status


        trade.setStatus(tradeDTO.getStatus());
        trade.setTransactionType(tradeDTO.getTransactionType());
        trade.setTradeBuyTime(LocalDateTime.now());
       trade.setCreatedBy("Dummy");
        trade.setCreatedDate(LocalDateTime.now());
        trade.setModifiedBy("Dummy");
        trade.setModifiedDate(LocalDateTime.now());
        trade.setLastModified(LocalDateTime.now());

        tradeRepository.save(trade);
        return tradeDTO;
    }

    public List<TradeDTO> getTradesByClientId(Long clientId) {
        List<Trade> trades = tradeRepository.findByClientId(clientId);
        return trades.stream()
                .map(TradeDTO::new)
                .collect(Collectors.toList());
    }

    public void deleteTrade(Long id) {

        tradeRepository.deleteById(id);
    }
    public List<TradeDTO> getTradesByClientIdAndSymbol(Long clientId, String stockSymbol) {
        List<Trade> tradeList = tradeRepository.findByClientIdAndStockSymbol(clientId, stockSymbol);
        return tradeList.stream()
                .map(TradeDTO::new)
                .collect(Collectors.toList());
    }
    public Map<String, Object> getTradeAndStockDetails(Long clientId, String symbol) {
        // Fetch all trades for the client and symbol
        List<Trade> trades = tradeRepository.findByClientIdAndStockSymbol(clientId, symbol);

        // Filter only executed trades
        List<Trade> executedTrades = trades.stream()
                .filter(t -> "EXECUTED".equalsIgnoreCase(String.valueOf(t.getStatus())))
                .toList();

        if (executedTrades.isEmpty()) {
            return Collections.emptyMap(); // No executed trades found
        }

//        // Fetch stock details
//        Optional<Stock> stockOpt = stockRepository.findBySymbol(symbol);
//        if (!stockOpt.isPresent()) {
//            return Collections.emptyMap();  // Stock not found
//        }
//
//        Stock stock = stockOpt.get();

        Queue<Trade> buyQueue = new LinkedList<>();
        double profit = 0.0;
        int totalExecutedQty = 0;

        for (Trade trade : executedTrades) {
            if (trade.getTransactionType() == TransactionType.BUY) {
                buyQueue.add(trade);
            } else if (trade.getTransactionType() == TransactionType.SELL) {
                int sellQty = trade.getQuantity();
                totalExecutedQty += sellQty;
                double sellPrice = trade.getPrice();

                while (sellQty > 0 && !buyQueue.isEmpty()) {
                    Trade buy = buyQueue.peek();
                    int buyQty = buy.getQuantity();

                    int matchedQty = Math.min(sellQty, buyQty);
                    profit += matchedQty * (sellPrice - buy.getPrice());

                    // update or remove buy
                    if (matchedQty == buyQty) {
                        buyQueue.poll();
                    } else {
                        buy.setQuantity(buyQty - matchedQty);
                    }

                    sellQty -= matchedQty;
                }
            }
        }

        Map<String, Object> result = new HashMap<>();
        result.put("stockSymbol", symbol);
        result.put("executedTradeCount", executedTrades.size());
        result.put("executedSellQuantity", totalExecutedQty);
        result.put("realizedProfit", profit);
        result.put("trades", executedTrades);  // Adding trade details to the response

        return result;
    }

//    @Transactional
//    public String executeTrade(TradeDTO tradeDTO) {
//        // Fetch stock details from the external service
//        Stock stock = stockServiceClient.getStockDetails(tradeDTO.getStockSymbol());
//
//        if (stock == null) {
//            return "Stock not found";
//        }
//
//        if (tradeDTO.getOrderType().equalsIgnoreCase("BUY")) {
//            return processBuyOrder(tradeDTO, stock);
//        } else if (tradeDTO.getOrderType().equalsIgnoreCase("SELL")) {
//            return processSellOrder(tradeDTO, stock);
//        } else {
//            return "Invalid order type";
//        }
//    }
//
//    private String processBuyOrder(TradeDTO tradeDTO, Stock stock) {
//        if (tradeDTO.getQuantity() <= 0) {
//            return "Invalid quantity";
//        }
//
//        if (tradeDTO.getPrice() <= 0) {
//            return "Invalid price";
//        }
//
//        // Process the buy order (simply creating the trade in this example)
//        Trade trade = new Trade();
//        trade.setClientId(tradeDTO.getClientId());
//        trade.setStockSymbol(tradeDTO.getStockSymbol());
//        trade.setQuantity(tradeDTO.getQuantity());
//        trade.setPrice(tradeDTO.getPrice());
//        trade.setOrderType("BUY");
//        trade.setStatus("PENDING");
//        trade.setTradeTime(LocalDateTime.now());
//
//        // Save the trade
//        tradeRepository.save(trade);
//
//        // You would not normally increase stock quantity after a buy in a real world system,
//        // but for this example, we'll assume stock availability can be adjusted in this service.
//        // Maybe update a user's portfolio or a centralized inventory in real-world scenarios.
//
//        return "Buy order placed successfully";
//    }
//
//    private String processSellOrder(TradeDTO tradeDTO, Stock stock) {
//        if (tradeDTO.getQuantity() <= 0) {
//            return "Invalid quantity";
//        }
//
//        if (tradeDTO.getPrice() <= 0) {
//            return "Invalid price";
//        }
//
//        // Check if the stock has enough quantity to sell
//        if (stock.getAvailableQuantity() < tradeDTO.getQuantity()) {
//            return "Not enough stock available to sell";
//        }
//
//        // Process the sell order
//        Trade trade = new Trade();
//        trade.setClientId(tradeDTO.getClientId());
//        trade.setStockSymbol(tradeDTO.getStockSymbol());
//        trade.setQuantity(tradeDTO.getQuantity());
//        trade.setPrice(tradeDTO.getPrice());
//        trade.setOrderType("SELL");
//        trade.setStatus("PENDING");
//        trade.setTradeTime(LocalDateTime.now());
//
//        // Save the trade
//        tradeRepository.save(trade);
//
//        // You would not normally decrease stock quantity directly from the trade service;
//        // you'd update the user's portfolio or an order book system.
//
//        return "Sell order placed successfully";
//    }

//    @Transactional
//    public String executeTrade(TradeDTO tradeDTO) {
//        Stock stock = stockRepository.findBySymbol(tradeDTO.getStockSymbol());
//
//        if (stock == null) {
//            return "Stock not found";
//        }
//
//        if (tradeDTO.getOrderType().equalsIgnoreCase("BUY")) {
//            return processBuyOrder(tradeDTO, stock);
//        } else if (tradeDTO.getOrderType().equalsIgnoreCase("SELL")) {
//            return processSellOrder(tradeDTO, stock);
//        } else {
//            return "Invalid order type";
//        }
//    }
//
//    private String processBuyOrder(TradeDTO tradeDTO, Stock stock) {
//        if (tradeDTO.getQuantity() <= 0) {
//            return "Invalid quantity";
//        }
//
//        if (tradeDTO.getPrice() <= 0) {
//            return "Invalid price";
//        }
//
//        // Place the buy order (assumes user has sufficient balance)
//        // Here we simply create the trade for demonstration.
//        Trade trade = new Trade();
//        trade.setClientId(tradeDTO.getClientId());
//        trade.setStockSymbol(tradeDTO.getStockSymbol());
//        trade.setQuantity(tradeDTO.getQuantity());
//        trade.setPrice(tradeDTO.getPrice());
//        trade.setOrderType("BUY");
//        trade.setStatus("PENDING");
//        trade.setTradeTime(LocalDateTime.now());
//
//        // Save the trade
//        tradeRepository.save(trade);
//
//        // Assuming we update the stock after a successful buy
//        stock.setAvailableQuantity(stock.getAvailableQuantity() + tradeDTO.getQuantity());
//        stockRepository.save(stock);
//
//        return "Buy order placed successfully";
//    }
//
//    private String processSellOrder(TradeDTO tradeDTO, Stock stock) {
//        if (tradeDTO.getQuantity() <= 0) {
//            return "Invalid quantity";
//        }
//
//        if (tradeDTO.getPrice() <= 0) {
//            return "Invalid price";
//        }
//
//        // Check if the stock has enough quantity to sell
//        if (stock.getAvailableQuantity() < tradeDTO.getQuantity()) {
//            return "Not enough stock available to sell";
//        }
//
//        // Place the sell order
//        Trade trade = new Trade();
//        trade.setClientId(tradeDTO.getClientId());
//        trade.setStockSymbol(tradeDTO.getStockSymbol());
//        trade.setQuantity(tradeDTO.getQuantity());
//        trade.setPrice(tradeDTO.getPrice());
//        trade.setOrderType("SELL");
//        trade.setStatus("PENDING");
//        trade.setTradeTime(LocalDateTime.now());
//
//        // Save the trade
//        tradeRepository.save(trade);
//
//        // Update the stock after a successful sell
//        stock.setAvailableQuantity(stock.getAvailableQuantity() - tradeDTO.getQuantity());
//        stockRepository.save(stock);
//
//        return "Sell order placed successfully";
//    }
}

