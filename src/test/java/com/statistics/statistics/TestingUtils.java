package com.statistics.statistics;

import com.statistics.statistics.exception.TransactionExpiredException;
import com.statistics.statistics.model.StatisticsSnapshot;
import com.statistics.statistics.model.Transaction;
import com.statistics.statistics.service.TransactionService;
import org.junit.Assert;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class TestingUtils {

    public static double AMOUNT_SEED = 1.0;
    public static long MILLISECONDS_BETWEEN_POSTING_TRANSACTIONS = 10;

    @Value("${statistics.api.window_length_in_milliseconds}")
    private long windowLengthInMilliseconds;

    public static void addXNumberOfTransactionsInOrder(TransactionService transactionService, int numberOfTransactions, TransactionPerRecordHandler transactionPerRecordHandler, long seedTransactionTime){

        for(int i = 0; i < numberOfTransactions; i++){

            try {
                long newTransactionTime = seedTransactionTime + (i * MILLISECONDS_BETWEEN_POSTING_TRANSACTIONS);

                Transaction transaction = new Transaction(newTransactionTime, AMOUNT_SEED * (i + 1));
                transactionService.addTransaction(transaction, newTransactionTime);
                StatisticsSnapshot statisticsSnapshot = transactionService.getResult(newTransactionTime);
                transactionPerRecordHandler.handler(statisticsSnapshot, newTransactionTime);
            } catch (TransactionExpiredException exception){
                System.out.println(exception.getStackTrace());
            }

        }

    }

    public interface TransactionPerRecordHandler {
        void handler(StatisticsSnapshot statisticsSnapshot, long timeToUse);
    }

}
