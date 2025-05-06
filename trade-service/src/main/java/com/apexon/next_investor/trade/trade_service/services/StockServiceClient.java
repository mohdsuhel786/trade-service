//package com.apexon.next_investor.trade.trade_service.services;
//import com.apexon.next_investor.trade.trade_service.entities.Stock;
//import org.springframework.beans.factory.annotation.Value;
//import org.springframework.stereotype.Service;
//import org.springframework.web.client.RestTemplate;
//
//@Service
//public class StockServiceClient {
//
//    private final RestTemplate restTemplate;
//    private final String stockServiceUrl;
//
//    // Injecting the URL of the Stock service from application.properties
//    public StockServiceClient(RestTemplate restTemplate,
//                              @Value("${stock.service.url}") String stockServiceUrl) {
//        this.restTemplate = restTemplate;
//        this.stockServiceUrl = stockServiceUrl;
//    }
//
//    // Fetch stock details for a given stock symbol
//    public Stock getStockDetails(String stockSymbol) {
//        String url = stockServiceUrl + "/api/stocks/" + stockSymbol;
//        return restTemplate.getForObject(url, Stock.class);
//    }
//}
