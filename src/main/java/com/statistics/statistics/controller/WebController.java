package com.statistics.statistics.controller;

import com.statistics.statistics.exception.TransactionExpiredDueToServerException;
import com.statistics.statistics.exception.TransactionExpiredException;
import com.statistics.statistics.model.StatisticsSnapshot;
import com.statistics.statistics.model.Transaction;
import com.statistics.statistics.service.TransactionServiceReverseProxy;

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
    TransactionServiceReverseProxy transactionServiceReverseProxy;

    @RequestMapping(value = "/transactions", method = RequestMethod.POST)
    public ResponseEntity<Void> postTransaction(@RequestBody Transaction transaction) {

        try{
            long currentTime = System.currentTimeMillis() / 1000L;
            transactionServiceReverseProxy.addTransaction(transaction, currentTime);
            return new ResponseEntity<>(HttpStatus.CREATED);
        } catch (TransactionExpiredException | TransactionExpiredDueToServerException e) {
            return new ResponseEntity<>(HttpStatus.NOT_ACCEPTABLE);
        }

    }

    @RequestMapping(value = "/statistics", method = RequestMethod.GET)
    public ResponseEntity<StatisticsSnapshot> getStatistics() {

        StatisticsSnapshot statisticsSnapshot = transactionServiceReverseProxy.getResult();
        return new ResponseEntity<>(statisticsSnapshot, HttpStatus.OK);

    }

}
