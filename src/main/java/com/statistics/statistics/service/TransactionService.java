package com.statistics.statistics.service;

import com.statistics.statistics.model.StatisticsSnapshot;
import com.statistics.statistics.model.Transaction;
import com.statistics.statistics.repository.StatisticsSnapshotRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;

@Service
public class TransactionService {

    @Autowired
    StatisticsSnapshotRepository statisticsSnapshotRepository;

    @Value("${statistics.api.window_length_in_milliseconds}")
    private long windowLengthInMilliseconds;

    public void addTransaction(Transaction transaction, Long currentTime) {

        StatisticsSnapshot newSnapshot = new StatisticsSnapshot(transaction.getTimestamp(),
                transaction.getAmount(), 1, transaction.getAmount(), transaction.getAmount(), transaction.getAmount());
        statisticsSnapshotRepository.addSnapShotEntry(transaction.getTimestamp(), newSnapshot);
        refreshRepository(currentTime);

    }

    private void refreshRepository(long currentTime){

        SortedMap<Long, StatisticsSnapshot> newStatisticsSortedMap = statisticsSnapshotRepository.getTempSnapShotSortedMap();
        updateSnapshots(currentTime, newStatisticsSortedMap);
        statisticsSnapshotRepository.setStatisticsSnapshotSortedMap(newStatisticsSortedMap);

    }

    private void updateSnapshots(long currentTime, SortedMap<Long, StatisticsSnapshot> newStatisticsSortedMap){

        List<StatisticsSnapshot> previousMinuteSnapshot = new ArrayList<>();
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
                previousMinuteSnapshot = priorEntry.getPreviousMinuteSnapshot();
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

        Map.Entry<Long, StatisticsSnapshot> lastSnapshotEntry = statisticsSnapshotRepository.getLastSnapshotEntry();
        lastSnapshotEntry.getValue().setPreviousMinuteSnapshot(previousMinuteSnapshot);

    }

    private void updateSnapshot(StatisticsSnapshot priorEntry, long startIndex,
                                long chunkSize, List<StatisticsSnapshot> previousMinuteSnapshot){

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

        entry.setPreviousMinuteSnapshot(new ArrayList<>());

    }

    private long getNewStartIndex(long startIndex, long priorTimestamp, long timeStamp, long currentTime){

        if(priorTimestamp != -1){
            startIndex = startIndex + (priorTimestamp - timeStamp);
        } else {
            startIndex = currentTime - timeStamp;
        }

        return startIndex;

    }

    public StatisticsSnapshot getResult(long endTime){

        Map.Entry<Long, StatisticsSnapshot> lastSnapshotEntry = statisticsSnapshotRepository.getLastSnapshotEntry();

        if(lastSnapshotEntry == null){
            return null;
        }

        Long timeStamp = lastSnapshotEntry.getKey();
        Long endOfWindow = endTime - windowLengthInMilliseconds;
        if(timeStamp < endOfWindow){
            return null;
        }

        Long lookBackIndex = windowLengthInMilliseconds - 1 - (endTime - timeStamp);

        StatisticsSnapshot lastSnapshotToInclude = statisticsSnapshotRepository.getOldestSnapshotInWindow(lookBackIndex);

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
