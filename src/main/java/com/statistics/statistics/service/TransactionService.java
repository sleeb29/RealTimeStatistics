package com.statistics.statistics.service;

import com.statistics.statistics.exception.TransactionExpiredDueToServerException;
import com.statistics.statistics.exception.TransactionExpiredException;
import com.statistics.statistics.model.StatisticsSnapshot;
import com.statistics.statistics.model.Transaction;
import com.statistics.statistics.repository.StatisticsSnapshotRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Map;
import java.util.SortedMap;

@Service
public class TransactionService {

    @Autowired
    StatisticsSnapshotRepository statisticsSnapshotRepository;

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

            StatisticsSnapshot newSnapshot = new StatisticsSnapshot(transaction.getTimestamp(),
                    transaction.getAmount(), 1, transaction.getAmount(), transaction.getAmount(), transaction.getAmount());
            statisticsSnapshotRepository.addSnapShotEntry(transaction.getTimestamp(), newSnapshot);

            //still want to pass in currentTime to guarantee determinism for invocations of addTransaction
            //if somehow the most recent post time was updated with a time after the current time use that
            long timeToUse = currentTime;
            if(mostRecentPostTime > 0 && mostRecentPostTime > currentTime){
                timeToUse = mostRecentPostTime;
            }

            refreshRepository(timeToUse);

            this.mostRecentPostTime = timeToUse;

        }

    }

    private void refreshRepository(long currentTime){

        SortedMap<Long, StatisticsSnapshot> newStatisticsSortedMap = statisticsSnapshotRepository.getTempSnapShotSortedMap();
        updateSnapshots(currentTime, newStatisticsSortedMap);
        statisticsSnapshotRepository.setStatisticsSnapshotSortedMap(newStatisticsSortedMap);

    }

    private void updateSnapshots(long currentTime, SortedMap<Long, StatisticsSnapshot> newStatisticsSortedMap){

        ArrayList<StatisticsSnapshot> previousMinuteSnapshot = new ArrayList<>();
        StatisticsSnapshot priorEntry = null;
        long startIndex = -1;
        long priorTimestamp = -1;
        SnapshotUpdateTracker snapshotUpdateTracker = new SnapshotUpdateTracker(0, Double.MAX_VALUE, Double.MIN_VALUE, 0.0);

        for(Map.Entry<Long, StatisticsSnapshot> entry : statisticsSnapshotRepository.getStatisticsSnapshotSortedMap().entrySet()){

            long timeStamp = entry.getKey();
            StatisticsSnapshot entrySnapshot = entry.getValue();

            Boolean isOutsideOfWindow = timeStamp < currentTime - windowLengthInMilliseconds;
            if(isOutsideOfWindow){
                break;
            }

            newStatisticsSortedMap.put(timeStamp, entrySnapshot);
            snapshotUpdateTracker.update(entrySnapshot);

            if(priorEntry != null) {
                updateSnapshot(priorEntry, startIndex, priorTimestamp - timeStamp, previousMinuteSnapshot);
            }

            priorEntry = entrySnapshot;
            updateEntry(priorEntry, snapshotUpdateTracker);

            startIndex = getNewStartIndex(startIndex, priorTimestamp, timeStamp, currentTime);
            priorTimestamp = timeStamp;

        }

        if(priorTimestamp != -1){
            long chunkSize = windowLengthInMilliseconds - (currentTime - priorTimestamp);
            updateSnapshot(priorEntry, startIndex, chunkSize, previousMinuteSnapshot);
        }

    }

    private void updateSnapshot(StatisticsSnapshot priorEntry, long startIndex,
                                long chunkSize, ArrayList<StatisticsSnapshot> previousMinuteSnapshot){

        for(long i = startIndex; i < startIndex + chunkSize; i++){
            previousMinuteSnapshot.add(priorEntry);
        }

        priorEntry.setPreviousMinuteSnapshot(previousMinuteSnapshot);

    }

    private void updateEntry(StatisticsSnapshot entry, SnapshotUpdateTracker snapshotUpdateTracker){

        entry.setSum(snapshotUpdateTracker.sum);
        entry.setMin(snapshotUpdateTracker.min);
        entry.setMax(snapshotUpdateTracker.max);
        entry.setCount(snapshotUpdateTracker.count);
        entry.setAvg(snapshotUpdateTracker.sum/snapshotUpdateTracker.count);

    }

    private long getNewStartIndex(long startIndex, long priorTimestamp, long timeStamp, long currentTime){

        if(priorTimestamp != -1){
            startIndex = startIndex + (priorTimestamp - timeStamp);
        } else {
            startIndex = currentTime - timeStamp;
        }

        return startIndex;

    }

    //need to synchronize on the entire instance of the TransactionService object here
    //as we don't want the repository modified while we retrieve it
    public synchronized StatisticsSnapshot getResult(long endTime){

        Map.Entry<Long, StatisticsSnapshot> lastSnapshotEntry = statisticsSnapshotRepository.getLastSnapshotEntry();

        if(lastSnapshotEntry == null){
            return null;
        }

        Long timeStamp = lastSnapshotEntry.getKey();
        Long endOfWindow = endTime - windowLengthInMilliseconds;
        if(timeStamp < endOfWindow){
            return null;
        }

        Long lookBackTime = endTime - timeStamp;

        StatisticsSnapshot lastSnapshotToInclude = statisticsSnapshotRepository.getOldestSnapshotInWindow(lookBackTime);

        return lastSnapshotToInclude;

    }

    public synchronized void flushTransactions(){
        statisticsSnapshotRepository.flush();
    }

    //convenience class - used to build object to be passed around to break up
    //the updating Snapshot logic
    private class SnapshotUpdateTracker {

        long count;
        Double min;
        Double max;
        Double sum;

        private SnapshotUpdateTracker(long count, Double min, Double max, Double sum){
            this.count = count;
            this.min = min;
            this.max = max;
            this.sum = sum;
        }

        private void update(StatisticsSnapshot entrySnapshot){
            if(entrySnapshot.getMin() < this.min){
                this.min = entrySnapshot.getMin();
            }

            if(entrySnapshot.getMax() > this.max){
                this.max = entrySnapshot.getMax();
            }

            this.sum += entrySnapshot.getAmount();
            this.count++;
        }

    }


}
