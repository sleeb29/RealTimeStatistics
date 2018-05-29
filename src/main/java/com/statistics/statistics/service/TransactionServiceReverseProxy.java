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

        Boolean acquiredLock = readWriteLock.writeLock().tryLock();
        while(!acquiredLock){
            acquiredLock = readWriteLock.writeLock().tryLock();
        }

        if(transaction.getTimestamp() < currentTime - windowLengthInMilliseconds){
            throw new TransactionExpiredException(transaction, transaction.getTimestamp());
        }

        transactionService.addTransaction(transaction, currentTime);

        readWriteLock.writeLock().unlock();

    }

    public StatisticsSnapshot getResult(long endTime) {

        Boolean acquiredLock = readWriteLock.readLock().tryLock();
        while(!acquiredLock){
            acquiredLock = readWriteLock.readLock().tryLock();
        }

        StatisticsSnapshot statisticsSnapshot = transactionService.getResult(endTime);

        readWriteLock.readLock().unlock();

        return statisticsSnapshot;

    }

}
