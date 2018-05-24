package com.statistics.statistics;

import com.statistics.statistics.exception.TransactionExpiredException;
import com.statistics.statistics.model.StatisticsSnapshot;
import com.statistics.statistics.model.Transaction;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.test.context.junit4.SpringRunner;

import com.statistics.statistics.service.TransactionService;

@RunWith(SpringRunner.class)
@ComponentScan({
        "com.statistics.statistics.service",
        "com.statistics.statistics.repository",
        "com.statistics.statistics.controller"
})
public class TransactionServiceTests {

    @Test
    public void addOneTransaction_ValidTransaction_True() throws TransactionExpiredException {

        TransactionService transactionService = new TransactionService();

        long currentTime = System.currentTimeMillis() / 1000L;
        Double amount = 12.0;

        Transaction transaction = new Transaction(currentTime, amount);
        transactionService.addTransaction(transaction, currentTime);

        StatisticsSnapshot statisticsSnapshot = transactionService.getResult(currentTime);

        Assert.assertNotNull(statisticsSnapshot);

        transactionService.flushTransactions();

    }

    @Test
    public void addTwoTransactions_SecondTransactionValid_True() throws TransactionExpiredException {

        TransactionService transactionService = new TransactionService();

        long transactionTime = System.currentTimeMillis() / 1000L;
        long secondTransactionTime = transactionTime + 1000;
        Double amount = 12.0;

        Transaction transaction = new Transaction(transactionTime, amount);
        transactionService.addTransaction(transaction, transactionTime);

        Transaction transaction2 = new Transaction(secondTransactionTime, amount);
        transactionService.addTransaction(transaction2, secondTransactionTime);

        StatisticsSnapshot statisticsSnapshot = transactionService.getResult(secondTransactionTime);

        Assert.assertNotNull(statisticsSnapshot);
        Assert.assertEquals(statisticsSnapshot.getTimestamp(), secondTransactionTime);

        transactionService.flushTransactions();

    }

}
