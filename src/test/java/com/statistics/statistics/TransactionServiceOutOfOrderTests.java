package com.statistics.statistics;

import com.statistics.statistics.exception.TransactionExpiredException;
import com.statistics.statistics.model.StatisticsSnapshot;
import com.statistics.statistics.model.Transaction;
import com.statistics.statistics.service.TransactionService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import org.junit.Assert;

@RunWith(SpringRunner.class)
@SpringBootTest
public class TransactionServiceOutOfOrderTests {

    @Autowired
    TransactionService transactionService;

    //@Test
    public void addTwoTransactions_AllTransactionsValid_True() throws TransactionExpiredException {

        long currentTimestamp = System.currentTimeMillis() / 1000L;

        TestingUtils.TransactionPerRecordHandler transactionPerRecordHandler = (statisticsSnapshot, newTransactionTime) -> {
            Assert.assertNotNull(statisticsSnapshot);
        };

        addXNumberOfTransactionsOutOfOrder(transactionService, 2, transactionPerRecordHandler, currentTimestamp);

        StatisticsSnapshot statisticsSnapshot = transactionService.getResult(currentTimestamp);
        Assert.assertNotNull(statisticsSnapshot);
        Assert.assertEquals(statisticsSnapshot.getAmount(), TestingUtils.AMOUNT_SEED * 2, 0.0);

        transactionService.flushTransactions();

    }

    //@Test
    public void addOneHundredTransactions_AllTransactionsValid_True() throws TransactionExpiredException {

        long currentTimestamp = System.currentTimeMillis() / 1000L;

        TestingUtils.TransactionPerRecordHandler transactionPerRecordHandler = (statisticsSnapshot, newTransactionTime) -> {
            Assert.assertNotNull(statisticsSnapshot);
        };

        addXNumberOfTransactionsOutOfOrder(transactionService, 100, transactionPerRecordHandler, currentTimestamp);

        StatisticsSnapshot statisticsSnapshot = transactionService.getResult(currentTimestamp);
        Assert.assertNotNull(statisticsSnapshot);
        Assert.assertEquals(statisticsSnapshot.getAmount(), TestingUtils.AMOUNT_SEED * 100, 0.0);

        transactionService.flushTransactions();

    }

    @Test
    public void addTwoTransactions_SecondTransactionValid_False() throws TransactionExpiredException {

        long transactionOneTimestamp = System.currentTimeMillis() / 1000L;
        long transactionTwoTimestamp = transactionOneTimestamp - transactionService.getWindowLengthInMilliseconds() - 100000;

        Transaction transactionOne = new Transaction(transactionOneTimestamp, TestingUtils.AMOUNT_SEED);
        Transaction transactionTwo = new Transaction(transactionTwoTimestamp, TestingUtils.AMOUNT_SEED * 2);

        transactionService.addTransaction(transactionOne, transactionOneTimestamp);
        transactionService.addTransaction(transactionTwo, transactionTwoTimestamp);

        StatisticsSnapshot statisticsSnapshot = transactionService.getResult(transactionOneTimestamp);
        Assert.assertNotNull(statisticsSnapshot);
        Assert.assertEquals(statisticsSnapshot.getAmount(), TestingUtils.AMOUNT_SEED, 0.0);
        Assert.assertEquals(statisticsSnapshot.getCount(), 2);

        transactionService.flushTransactions();

    }

    private void addXNumberOfTransactionsOutOfOrder(TransactionService transactionService, int numberOfTransactions, TestingUtils.TransactionPerRecordHandler transactionPerRecordHandler, long seedTransactionTime) throws TransactionExpiredException {

        for(int i = 0; i < numberOfTransactions; i++){

            long newTransactionTime = seedTransactionTime - (i * TestingUtils.MILLISECONDS_BETWEEN_POSTING_TRANSACTIONS);
            Transaction transaction = new Transaction(newTransactionTime, TestingUtils.AMOUNT_SEED * (i + 1));
            transactionService.addTransaction(transaction, newTransactionTime);
            StatisticsSnapshot statisticsSnapshot = transactionService.getResult(newTransactionTime);
            transactionPerRecordHandler.handler(statisticsSnapshot, newTransactionTime);

        }

    }


}
