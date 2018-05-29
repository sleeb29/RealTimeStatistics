package com.statistics.statistics.service;

import com.statistics.statistics.exception.TransactionExpiredDueToServerException;
import com.statistics.statistics.exception.TransactionExpiredException;
import com.statistics.statistics.model.StatisticsSnapshot;
import com.statistics.statistics.model.Transaction;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

@Component
public class TransactionServiceReverseProxy {

    @Autowired
    TransactionService transactionService;

    @Value("${statistics.api.window_length_in_milliseconds}")
    private long windowLengthInMilliseconds;
    private ReadWriteLock readWriteLock;

    @PostConstruct
    private void init(){
        this.readWriteLock = new ReentrantReadWriteLock();
    }

    public void addTransaction(Transaction transaction, Long currentTime) throws TransactionExpiredException, TransactionExpiredDueToServerException {

        //return immediately, still check after we acquire the lock though in case
        //the server could not handle the transaction
        if(transaction.getTimestamp() < currentTime - windowLengthInMilliseconds){
            throw new TransactionExpiredException(transaction, transaction.getTimestamp());
        }

        Boolean acquiredLock = readWriteLock.writeLock().tryLock();
        while(!acquiredLock){
            acquiredLock = readWriteLock.writeLock().tryLock();
        }

        //test that the thread wasn't blocked for too long
        //checking here instead of letting the underlying service through the error
        //so we can log that the post was originally within the window but due to inability
        //to process transaction in time had to discard it
        long realCurrentTime = System.currentTimeMillis() / 1000L;
        if(transaction.getTimestamp() < realCurrentTime - windowLengthInMilliseconds){
            throw new TransactionExpiredDueToServerException(transaction, transaction.getTimestamp(), realCurrentTime, currentTime);
        }

        transactionService.addTransaction(transaction, currentTime);

        readWriteLock.writeLock().unlock();

    }

    public StatisticsSnapshot getResult() {

        Boolean acquiredLock = readWriteLock.readLock().tryLock();
        while(!acquiredLock){
            acquiredLock = readWriteLock.readLock().tryLock();
        }

        //allows for grabbing the current snapshot after acquiring the lock
        long currentTime = System.currentTimeMillis() / 1000L;

        StatisticsSnapshot statisticsSnapshot = transactionService.getResult(currentTime);

        readWriteLock.readLock().unlock();

        return statisticsSnapshot;

    }

}
