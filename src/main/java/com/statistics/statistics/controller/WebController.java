package com.statistics.statistics.controller;

import com.statistics.statistics.exception.TransactionExpiredExeption;
import com.statistics.statistics.model.StatisticsSnapshot;
import com.statistics.statistics.model.Transaction;

import com.statistics.statistics.service.TransactionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class WebController {

    @Autowired
    TransactionService transactionService;

    @RequestMapping(value = "/transactions", method = RequestMethod.POST)
    public ResponseEntity<Void> postTransaction(@RequestBody Transaction transaction) {

        try{
            long currentTime = System.currentTimeMillis() / 1000L;
            transactionService.addTransaction(transaction, currentTime);
            return new ResponseEntity<>(HttpStatus.CREATED);
        } catch (TransactionExpiredExeption e) {
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        }

    }

    @RequestMapping(value = "/statistics", method = RequestMethod.GET)
    public ResponseEntity<Void> getStatistics() {

        long endTime = System.currentTimeMillis() / 1000L;
        StatisticsSnapshot statisticsSnapshot = transactionService.getResult(endTime);
        return null;
    }

}
