package com.statistics.statistics.service;

import com.statistics.statistics.exception.TransactionExpiredExeption;
import com.statistics.statistics.model.StatisticsSnapshot;
import com.statistics.statistics.model.Transaction;
import com.statistics.statistics.repository.StatisticsSnapshotRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Map;
import java.util.SortedMap;

@Service
public class TransactionService {

    @Autowired
    StatisticsSnapshotRepository statisticsSnapshotRepository;

    static long WINDOW_IN_MILLISECONDS = 60_000;

    public synchronized void addTransaction(Transaction transaction, Long currentTime) throws TransactionExpiredExeption {

        Map.Entry<Long, StatisticsSnapshot> lastSnapshotEntry = statisticsSnapshotRepository.getLastSnapshotEntry();
        if(lastSnapshotEntry == null){
            StatisticsSnapshot statisticsSnapshot = new StatisticsSnapshot(transaction.getTimestamp(),
                    transaction.getAmount(), 1 , transaction.getAmount(), transaction.getAmount(), transaction.getAmount());
            statisticsSnapshotRepository.addSnapShotEntry(transaction.getTimestamp(), statisticsSnapshot);
            return;
        }

        if(transaction.getTimestamp() < currentTime - WINDOW_IN_MILLISECONDS){
            throw new TransactionExpiredExeption(transaction, transaction.getTimestamp());
        }

        StatisticsSnapshot newSnapshot = new StatisticsSnapshot(transaction.getTimestamp(),
                transaction.getAmount(), 1 , transaction.getAmount(), transaction.getAmount(), transaction.getAmount());
        statisticsSnapshotRepository.addSnapShotEntry(transaction.getTimestamp(), newSnapshot);

        refreshRepository(currentTime);

    }

    private void refreshRepository(long currentTime){

        Map.Entry<Long, StatisticsSnapshot> lastSnapshotEntry = statisticsSnapshotRepository.getLastSnapshotEntry();

        StatisticsSnapshot lastSnapshot = lastSnapshotEntry.getValue();
        Long snapShotTimestamp = lastSnapshotEntry.getKey();

        SortedMap<Long, StatisticsSnapshot> newStatisticsSortedMap = statisticsSnapshotRepository.getTempSnapShotSortedMap();

        long count = 1;
        Double sum = lastSnapshot.getAmount(), min = lastSnapshot.getAmount(), max = lastSnapshot.getAmount();
        StatisticsSnapshot priorEntry = null;

        ArrayList<StatisticsSnapshot> previousMinuteSnapshot = new ArrayList<>();

        long lastMillisecond = snapShotTimestamp;

        for(Map.Entry<Long, StatisticsSnapshot> entry : statisticsSnapshotRepository.getStatisticsSnapshotSortedMap().entrySet()){

            long timeStamp = entry.getKey();
            StatisticsSnapshot entrySnapshot = entry.getValue();

            if(entrySnapshot.equals(lastSnapshotEntry)){
                continue;
            }

            if(timeStamp >= currentTime - WINDOW_IN_MILLISECONDS){

                newStatisticsSortedMap.put(timeStamp, entrySnapshot);

                if(entrySnapshot.getMin() < min){
                    min = entrySnapshot.getMin();
                } else if(entrySnapshot.getMax() > max){
                    max = entrySnapshot.getMax();
                }

                sum += entrySnapshot.getAmount();
                count++;

                if(priorEntry != null){
                    for(long i = lastMillisecond; i < timeStamp - snapShotTimestamp; i++){
                        previousMinuteSnapshot.add(priorEntry);
                    }
                }

                priorEntry = entrySnapshot;
                priorEntry.setSum(sum);
                priorEntry.setMin(min);
                priorEntry.setMax(max);
                priorEntry.setCount(count);
                priorEntry.setAvg(sum/count);

                lastMillisecond = timeStamp - snapShotTimestamp;

            } else {
                break;
            }
        }

        if(priorEntry != null){
            for(long i = lastMillisecond - 1; i < priorEntry.getTimestamp() - snapShotTimestamp; i++){
                previousMinuteSnapshot.add(priorEntry);
            }
        }

        lastMillisecond =  priorEntry.getTimestamp() - snapShotTimestamp;

        StatisticsSnapshot newSnapshot = new StatisticsSnapshot(snapShotTimestamp,
                sum, count, lastSnapshot.getAmount(), min, max);
        newSnapshot.setAvg(sum/count);
        newSnapshot.setPreviousMinuteSnapshot(previousMinuteSnapshot);

        for(long i = 0; i < lastMillisecond; i++){
            previousMinuteSnapshot.add(newSnapshot);
        }

        newStatisticsSortedMap.put(snapShotTimestamp, newSnapshot);
        statisticsSnapshotRepository.setStatisticsSnapshotSortedMap(newStatisticsSortedMap);

    }

    public synchronized StatisticsSnapshot getResult(long endTime){

        Map.Entry<Long, StatisticsSnapshot> lastSnapshotEntry = statisticsSnapshotRepository.getLastSnapshotEntry();

        if(lastSnapshotEntry == null){
            return null;
        }

        Long timeStamp = lastSnapshotEntry.getKey();
        Long lookBackTime = endTime - WINDOW_IN_MILLISECONDS;
        if(timeStamp < lookBackTime){
            return null;
        }

        StatisticsSnapshot lastSnapshotToInclude = statisticsSnapshotRepository.getOldestSnapshotInWindow(lookBackTime);

        return lastSnapshotToInclude;

    }

}
