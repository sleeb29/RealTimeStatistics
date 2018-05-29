package com.statistics.statistics.service;

import com.statistics.statistics.exception.TransactionExpiredDueToServerException;
import com.statistics.statistics.exception.TransactionExpiredException;
import com.statistics.statistics.model.StatisticsSnapshot;
import com.statistics.statistics.model.Transaction;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class TransactionServiceReverseProxy {

    @Autowired
    TransactionService transactionService;

    @Value("${statistics.api.window_length_in_milliseconds}")
    private long windowLengthInMilliseconds;
    private volatile long mostRecentPostTime;

    public void addTransaction(Transaction transaction, Long currentTime) throws TransactionExpiredException, TransactionExpiredDueToServerException {

        if(transaction.getTimestamp() < currentTime - windowLengthInMilliseconds){
            throw new TransactionExpiredException(transaction, transaction.getTimestamp());
        }

        synchronized(this) {

            //check again with new current time in synchronized block in case thread was
            //blocked from executing this code for too long and is now actually outside the window
            long realCurrentTime = System.currentTimeMillis() / 1000L;
            if(transaction.getTimestamp() < realCurrentTime - windowLengthInMilliseconds){
                throw new TransactionExpiredDueToServerException(transaction, transaction.getTimestamp(), realCurrentTime, currentTime);
            }

            transactionService.addTransaction(transaction, realCurrentTime);

            this.mostRecentPostTime = realCurrentTime;

        }

    }

    //need to synchronize on the entire instance of the TransactionServiceReverseProxy object here
    //as we don't want the repository modified while we retrieve it
    public synchronized StatisticsSnapshot getResult(long endTime) {
        return transactionService.getResult(endTime);
    }

}
