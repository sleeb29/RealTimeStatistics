package com.statistics.statistics.exception;

import com.statistics.statistics.model.Transaction;

public class TransactionExpiredDueToServerException extends Exception {

    public TransactionExpiredDueToServerException() {}

    public TransactionExpiredDueToServerException(Transaction transaction, long comparedTimestamp, long currentTime, long originalCurrentTime){
        super("SERVER HELD ON TOO LONG: Could not post transaction with stats: timestamp " + transaction.getTimestamp() + " - amount " + transaction.getAmount() +
        " at " + comparedTimestamp + " original server time: " + originalCurrentTime +
        " server time when tried to post: " + currentTime);
    }

}
