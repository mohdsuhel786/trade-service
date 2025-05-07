package com.apexon.next_investor.trade.trade_service.services;

import com.apexon.next_investor.trade.trade_service.dtos.AggregatedTradeDTO;
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

//    public List<TradeDTO> getTradesByClientId(Long clientId) {
//        List<Trade> trades = tradeRepository.findByClientId(clientId);
//        return trades.stream()
//                .map(TradeDTO::new)
//                .collect(Collectors.toList());
//    }

    public void deleteTrade(Long id) {

        tradeRepository.deleteById(id);
    }

    public Map<String, Object> getTradeAndStockDetails(Long clientId) {
        // Fetch all trades for the client
        List<Trade> trades = tradeRepository.findByClientId(clientId);

        // Filter only executed trades
        List<Trade> executedTrades = trades.stream()
                .filter(t -> "EXECUTED".equalsIgnoreCase(String.valueOf(t.getStatus())))
                .toList();

        if (executedTrades.isEmpty()) {
            return Collections.emptyMap(); // No executed trades found
        }

        // Group trades by stock symbol
        Map<String, List<Trade>> groupedTrades = executedTrades.stream()
                .collect(Collectors.groupingBy(Trade::getStockSymbol));

        Map<String, Object> result = new HashMap<>();

        // Process each stock symbol group
        for (Map.Entry<String, List<Trade>> entry : groupedTrades.entrySet()) {
            String stockSymbol = entry.getKey();
            List<Trade> symbolTrades = entry.getValue();

            Queue<TradeDTO> buyQueue = new LinkedList<>();
            double profit = 0.0;
            int totalExecutedBuyQty = 0;
            int totalExecutedSellQty = 0;
            int remainingBuyQty = 0;
            double totalBuyAmount = 0.0;  // To track total buy cost
            double totalSellRevenue = 0.0;  // To track total sell revenue
            double totalBuyCost = 0.0;  // To track the actual buy cost for purchase value

            // Sort trades by creation time (optional for FIFO matching)
            symbolTrades.sort(Comparator.comparing(Trade::getCreatedDate));

            // Process each trade for the current stock symbol
            for (Trade trade : symbolTrades) {
                if (trade.getTransactionType() == TransactionType.BUY) {
                    totalExecutedBuyQty += trade.getQuantity();
                    totalBuyCost += trade.getQuantity() * trade.getPrice(); // Add buy cost
                    totalBuyAmount += trade.getQuantity() * trade.getPrice(); // Total amount spent on buys
                    buyQueue.add(new TradeDTO(trade)); // Copy to avoid modifying original
                } else if (trade.getTransactionType() == TransactionType.SELL) {
                    int sellQty = trade.getQuantity();
                    double sellPrice = trade.getPrice();

                    while (sellQty > 0 && !buyQueue.isEmpty()) {
                        TradeDTO buy = buyQueue.peek();
                        int buyQty = buy.getQuantity();

                        int matchedQty = Math.min(sellQty, buyQty);
                        totalExecutedSellQty += matchedQty;

                        profit += matchedQty * (sellPrice - buy.getPrice());

                        totalSellRevenue += matchedQty * sellPrice; // Add to total revenue from sell trades

                        // Update or remove the buy trade from the queue
                        if (matchedQty == buyQty) {
                            buyQueue.poll(); // Fully matched, remove buy trade
                        } else {
                            buy.setQuantity(buyQty - matchedQty); // Partially matched, reduce quantity
                        }

                        sellQty -= matchedQty;
                    }

                    // If there's any remaining SELL qty, it's ignored (no matched buys)
                }
            }

            // Any unmatched buys are still in the queue
            remainingBuyQty = buyQueue.stream().mapToInt(TradeDTO::getQuantity).sum();

            // Calculate the purchase value (net cost)
            double purchaseValue = totalBuyAmount - totalSellRevenue;

            // Calculate the average buy price (moving average price)
            double averageBuyPrice = totalExecutedBuyQty > 0 ? totalBuyCost / totalExecutedBuyQty : 0.0;

            // Prepare the result for this stock symbol
            Map<String, Object> symbolResult = new HashMap<>();
            symbolResult.put("executedTradeCount", symbolTrades.size());
            symbolResult.put("executedBuyQuantity", totalExecutedBuyQty);
            symbolResult.put("executedSellQuantity", totalExecutedSellQty);
            symbolResult.put("realizedProfit", profit);
            symbolResult.put("remainingQuantity", remainingBuyQty);  // Remaining unmatched buys
            symbolResult.put("purchaseValue", purchaseValue);  // Net cost of purchases
            symbolResult.put("averageBuyPrice", averageBuyPrice);  // Moving average buy price
            //symbolResult.put("trades", symbolTrades);

            // Add this stock symbol result to the final result map
            result.put(stockSymbol, symbolResult);
        }

        return result;
    }



//    public Map<String, Object> getTradeAndStockDetails(Long clientId) {
//        // Fetch all trades for the client
//        List<Trade> trades = tradeRepository.findByClientId(clientId);
//
//        List<TradeDTO> tradeDTOS = trades.stream()
//                .map(TradeDTO::new)
//                .toList();
//        // Filter only executed trades
//        List<TradeDTO> executedTrades = tradeDTOS.stream()
//                .filter(t -> "EXECUTED".equalsIgnoreCase(String.valueOf(t.getStatus())))
//                .collect(Collectors.toList());
//
//        if (executedTrades.isEmpty()) {
//            return Collections.emptyMap(); // No executed trades found
//        }
//
//        // Group the executed trades by stock symbol
//        Map<String, List<TradeDTO>> groupedTrades = executedTrades.stream()
//                .collect(Collectors.groupingBy(TradeDTO::getStockSymbol));
//
//        Map<String, Object> result = new HashMap<>();
//
//        // For each stock symbol, calculate the total quantity, profit, etc.
//        for (Map.Entry<String, List<TradeDTO>> entry : groupedTrades.entrySet()) {
//            String stockSymbol = entry.getKey();
//            List<TradeDTO> symbolTrades = entry.getValue();
//
//            Queue<TradeDTO> buyQueue = new LinkedList<>();
//            double profit = 0.0;
//            int totalExecutedQty = 0;
//
//            // Calculate profit and total quantities
//            for (TradeDTO trade : symbolTrades) {
//                if (trade.getTransactionType() == TransactionType.BUY) {
//                    buyQueue.add(trade);
//                } else if (trade.getTransactionType() == TransactionType.SELL) {
//                    int sellQty = trade.getQuantity();
//                    totalExecutedQty += sellQty;
//                    double sellPrice = trade.getPrice();
//
//                    while (sellQty > 0 && !buyQueue.isEmpty()) {
//                        TradeDTO buy = buyQueue.peek();
//                        int buyQty = buy.getQuantity();
//
//                        int matchedQty = Math.min(sellQty, buyQty);
//                        profit += matchedQty * (sellPrice - buy.getPrice());
//
//                        // Update or remove buy trade from queue
//                        if (matchedQty == buyQty) {
//                            buyQueue.poll();
//                        } else {
//                            buy.setQuantity(buyQty - matchedQty);
//                        }
//
//                        sellQty -= matchedQty;
//                    }
//                }
//            }
//
//            // Create a result map for the current stock symbol
//            Map<String, Object> stockResult = new HashMap<>();
//            stockResult.put("executedTradeCount", symbolTrades.size());
//            stockResult.put("executedSellQuantity", totalExecutedQty);
//            stockResult.put("realizedProfit", profit);
//            stockResult.put("trades", symbolTrades);
//
//            // Put it into the result map
//            result.put(stockSymbol, stockResult);
//        }
//
//        return result;
//    }


//    public List<AggregatedTradeDTO> getAggregatedTradesByClientId(Long clientId) {
//        List<Trade> trades = tradeRepository.findByClientId(clientId);
//
//        return trades.stream()
//                .collect(Collectors.groupingBy(
//                        Trade::getStockSymbol,
//                        Collectors.collectingAndThen(Collectors.toList(), tradeGroup -> {
//                            int totalQuantity = tradeGroup.stream()
//                                    .mapToInt(Trade::getQuantity)
//                                    .sum();
//                            double totalValue = tradeGroup.stream()
//                                    .mapToDouble(t -> t.getQuantity() * t.getPrice())
//                                    .sum();
//                            double averagePrice = totalQuantity > 0 ? totalValue / totalQuantity : 0;
//
//                            return new AggregatedTradeDTO(
//                                    tradeGroup.get(0).getStockSymbol(),
//                                    totalQuantity,
//                                    averagePrice,
//                                    totalValue
//                            );
//                        })
//                ))
//                .values()
//                .stream()
//                .collect(Collectors.toList());
//    }



    //    public List<TradeDTO> getTradesByClientIdAndSymbol(Long clientId, String stockSymbol) {
//        List<Trade> tradeList = tradeRepository.findByClientIdAndStockSymbol(clientId, stockSymbol);
//
//
//
//        return tradeList.stream()
//                .map(TradeDTO::new)
//                .collect(Collectors.toList());
//    }
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

