package com.statistics.statistics.exception;

import com.statistics.statistics.model.Transaction;

public class TransactionExpiredExeption extends Exception {

    public TransactionExpiredExeption() {}

    public TransactionExpiredExeption(Transaction transaction, long comparedTimestamp){
        super("Could not post transaction with stats: timestamp " + transaction.getTimestamp() + " - amount " + transaction.getAmount() +
        " at " + comparedTimestamp);
    }

}
