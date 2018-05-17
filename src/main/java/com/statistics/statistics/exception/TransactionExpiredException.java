package com.statistics.statistics.exception;

import com.statistics.statistics.model.Transaction;

public class TransactionExpiredException extends Exception {

    public TransactionExpiredException() {}

    public TransactionExpiredException(Transaction transaction, long comparedTimestamp){
        super("Could not post transaction with stats: timestamp " + transaction.getTimestamp() + " - amount " + transaction.getAmount() +
        " at " + comparedTimestamp);
    }

}
