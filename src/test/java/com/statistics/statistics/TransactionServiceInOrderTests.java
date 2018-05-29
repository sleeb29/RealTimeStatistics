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
public class TransactionServiceInOrderTests {

    @Autowired
    TransactionService transactionService;

    @Test
    public void addOneTransaction_ValidTransaction_True() {
        TestingUtils.callAddXNumberOfTransactionsInOrder(transactionService,1);
    }

    @Test
    public void addTenTransactions_AllTransactionsValid_True() {
        TestingUtils.callAddXNumberOfTransactionsInOrder(transactionService,10);
    }

    @Test
    public void addTenThousandTransactions_AllTransactionsValid_True() {
        TestingUtils.callAddXNumberOfTransactionsInOrder(transactionService,10_000);
    }

    @Test
    public void addOneHundredThousandTransactions_AllTransactionsValid_True() {
        TestingUtils.callAddXNumberOfTransactionsInOrder(transactionService,100_000);
    }

    @Test
    public void getStatisticsSnapshotTwoTransactions_StatisticsSnapshotCorrect_True(){

        Double transactionOneAmount = 12.0;
        Double transactionTwoAmount = 22.0;

        long transactionOneTime = System.currentTimeMillis() / 1000L;

        Transaction transactionOne = new Transaction(transactionOneTime, transactionOneAmount);

        transactionService.addTransaction(transactionOne, transactionOneTime);

        Double expectedMax = Math.max(transactionOneAmount, transactionTwoAmount);
        Double expectedMin = Math.min(transactionOneAmount, transactionTwoAmount);
        long expectedCount = 2;
        Double expectedAvg = (transactionOneAmount + transactionTwoAmount)/expectedCount;

        long transactionTwoTime = System.currentTimeMillis() / 1000L + 10;

        Transaction transactionTwo = new Transaction(transactionTwoTime, transactionTwoAmount);

        transactionService.addTransaction(transactionTwo, transactionTwoTime);

        StatisticsSnapshot statisticsSnapshot = transactionService.getResult(transactionTwoTime);

        Assert.assertNotNull(statisticsSnapshot);
        Assert.assertEquals(statisticsSnapshot.getMax(), expectedMax, 0.0);
        Assert.assertEquals(statisticsSnapshot.getMin(), expectedMin, 0.0);
        Assert.assertEquals(statisticsSnapshot.getAvg(), expectedAvg, 0.0);
        Assert.assertEquals(statisticsSnapshot.getCount(), expectedCount);

    }

}
