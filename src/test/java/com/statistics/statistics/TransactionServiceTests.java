package com.statistics.statistics;

import com.statistics.statistics.model.StatisticsSnapshot;
import com.statistics.statistics.model.Transaction;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import com.statistics.statistics.service.TransactionService;

@RunWith(SpringRunner.class)
@SpringBootTest
public class TransactionServiceTests {

    @Autowired
    TransactionService transactionService;

    /*
        In Order Transactions
     */

    @Test
    public void addOneTransactionInTimeOrder_ValidTransaction_True() {

        TransactionPerRecordHandler transactionPerRecordHandler = (statisticsSnapshot, newTransactionTime) -> {
            Assert.assertNotNull(statisticsSnapshot);
            Assert.assertEquals(statisticsSnapshot.getTimestamp(), newTransactionTime);
        };

        addXNumberOfTransactionsInTimeOrder(1, transactionPerRecordHandler);

    }

    @Test
    public void addTenTransactionsInTimeOrder_AllTransactionsValid_True() {

        TransactionPerRecordHandler transactionPerRecordHandler = (statisticsSnapshot, newTransactionTime) -> {
            Assert.assertNotNull(statisticsSnapshot);
            Assert.assertEquals(statisticsSnapshot.getTimestamp(), newTransactionTime);
        };

        addXNumberOfTransactionsInTimeOrder(10, transactionPerRecordHandler);

    }

    @Test
    public void addTenThousandTransactionsInTimeOrder_AllTransactionsValid_True() {

        TransactionPerRecordHandler transactionPerRecordHandler = (statisticsSnapshot, newTransactionTime) -> {
            Assert.assertNotNull(statisticsSnapshot);
            Assert.assertEquals(statisticsSnapshot.getTimestamp(), newTransactionTime);
        };

        addXNumberOfTransactionsInTimeOrder(10_000, transactionPerRecordHandler);

    }

    @Test
    public void addOneHundredThousandTransactionsInTimeOrder_AllTransactionsValid_True() {

        TransactionPerRecordHandler transactionPerRecordHandler = (statisticsSnapshot, newTransactionTime) -> {
            Assert.assertNotNull(statisticsSnapshot);
            Assert.assertEquals(statisticsSnapshot.getTimestamp(), newTransactionTime);
        };

        addXNumberOfTransactionsInTimeOrder(100_000, transactionPerRecordHandler);

    }

    private void addXNumberOfTransactionsInTimeOrder(int numberOfTransactions, TransactionPerRecordHandler transactionPerRecordHandler){

        Double amount = 12.0;
        int counter = 0;
        long transactionTime = System.currentTimeMillis() / 1000L;

        while(counter < numberOfTransactions){

            long newTransactionTime = transactionTime + (counter * 100);
            Transaction transaction = new Transaction(newTransactionTime, amount);
            transactionService.addTransaction(transaction, newTransactionTime);
            StatisticsSnapshot statisticsSnapshot = transactionService.getResult(newTransactionTime);
            transactionPerRecordHandler.handler(statisticsSnapshot, newTransactionTime);

            counter++;

        }

        transactionService.flushTransactions();

    }

    private interface TransactionPerRecordHandler {
        void handler(StatisticsSnapshot statisticsSnapshot, long timeToUse);
    }

}
