package com.statistics.statistics.model;

public class Transaction {

    double amount;
    long timestamp;

    public Transaction(){

    }

    public Transaction(long timestamp, double amount){
        this.timestamp = timestamp;
        this.amount = amount;
    }

    public double getAmount() {
        return amount;
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }
}
