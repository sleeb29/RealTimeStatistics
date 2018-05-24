package com.statistics.statistics.service;

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

    public TransactionService(){

    }

    @Value("${statistics.api.window_length_in_milliseconds}")
    private long windowLengthInMilliseconds;

    public synchronized void addTransaction(Transaction transaction, Long currentTime) throws TransactionExpiredException {

        if(transaction.getTimestamp() < currentTime - windowLengthInMilliseconds){
            throw new TransactionExpiredException(transaction, transaction.getTimestamp());
        }

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

        ArrayList<StatisticsSnapshot> previousMinuteSnapshot = new ArrayList<>();

        long count = 0;
        Double sum = 0.0, min = Double.MAX_VALUE, max = Double.MIN_VALUE;
        StatisticsSnapshot priorEntry = null;
        long startIndex = -1;
        long priorTimestamp = -1;

        for(Map.Entry<Long, StatisticsSnapshot> entry : statisticsSnapshotRepository.getStatisticsSnapshotSortedMap().entrySet()){

            long timeStamp = entry.getKey();
            StatisticsSnapshot entrySnapshot = entry.getValue();

            if(timeStamp < currentTime - windowLengthInMilliseconds){
                break;
            }

            newStatisticsSortedMap.put(timeStamp, entrySnapshot);

            if(entrySnapshot.getMin() < min){
                min = entrySnapshot.getMin();
            }

            if(entrySnapshot.getMax() > max){
                max = entrySnapshot.getMax();
            }

            sum += entrySnapshot.getAmount();
            count++;

            if(priorEntry != null) {
                updateSnapshot(priorEntry, startIndex, priorTimestamp - timeStamp, previousMinuteSnapshot);
            }


            priorEntry = entrySnapshot;

            updateEntry(priorEntry, sum, min, max, count);

            if(priorTimestamp != -1){
                startIndex = startIndex + (priorTimestamp - timeStamp);
            } else {
                startIndex = currentTime - timeStamp;
            }

            priorTimestamp = timeStamp;

        }

        if(startIndex != -1){
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

    private void updateEntry(StatisticsSnapshot entry, Double sum, Double min, Double max, long count){
        entry.setSum(sum);
        entry.setMin(min);
        entry.setMax(max);
        entry.setCount(count);
        entry.setAvg(sum/count);
    }

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


}
