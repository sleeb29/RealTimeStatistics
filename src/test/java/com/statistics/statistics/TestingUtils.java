package com.statistics.statistics;

import com.statistics.statistics.model.StatisticsSnapshot;
import com.statistics.statistics.model.Transaction;
import com.statistics.statistics.service.TransactionService;

public class TestingUtils {

    public static void addXNumberOfTransactions(TransactionService transactionService, int numberOfTransactions, TransactionPerRecordHandler transactionPerRecordHandler){

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

    public interface TransactionPerRecordHandler {
        void handler(StatisticsSnapshot statisticsSnapshot, long timeToUse);
    }

}
