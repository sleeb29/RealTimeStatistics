package com.statistics.statistics.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;

@Component
public class StatisticsSnapshot {

    double avg;
    double sum;
    double min;
    double max;
    double amount;

    long count;
    long timestamp;

    List<StatisticsSnapshot> previousMinuteSnapshot;

    public StatisticsSnapshot(){

    }

    public StatisticsSnapshot(long timestamp, double sum, long count, double amount, double min, double max){

        this.timestamp = timestamp;
        this.sum = sum;
        this.count = count;
        this.amount = amount;
        this.max = max;
        this.min = min;

    }

    @JsonIgnore
    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public double getMin() {
        return min;
    }

    public void setMin(double min) {
        this.min = min;
    }

    public double getMax() {
        return max;
    }

    public void setMax(double max) {
        this.max = max;
    }

    public double getSum() {
        return sum;
    }

    public void setSum(double sum) {
        this.sum = sum;
    }

    public long getCount() {
        return count;
    }

    public void setCount(long count) {
        this.count = count;
    }

    public double getAvg() {
        return avg;
    }

    public void setAvg(double avg) {
        this.avg = avg;
    }

    @JsonIgnore
    public double getAmount() {
        return amount;
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }

    public List<StatisticsSnapshot> getPreviousMinuteSnapshot() {
        if(this.previousMinuteSnapshot == null){
            return new ArrayList<>();
        }
        return this.previousMinuteSnapshot;
    }

    @JsonIgnore
    public void setPreviousMinuteSnapshot(List<StatisticsSnapshot> previousMinuteSnapshot) {
        this.previousMinuteSnapshot = previousMinuteSnapshot;
    }

}
