package com.statistics.statistics.repository;

import com.statistics.statistics.model.StatisticsSnapshot;
import org.springframework.stereotype.Repository;

import javax.annotation.PostConstruct;
import java.util.*;

@Repository
public class StatisticsSnapshotRepository {

    SortedMap<Long, StatisticsSnapshot> statisticsSnapshotSortedMap;

    @PostConstruct
    private void initStatisticsSnapshotSortedMap() {
        this.statisticsSnapshotSortedMap = new TreeMap<>(new TransactionTimeStampComparator());
    }

    public SortedMap<Long, StatisticsSnapshot> getStatisticsSnapshotSortedMap() {
        return statisticsSnapshotSortedMap;
    }

    public void setStatisticsSnapshotSortedMap(SortedMap<Long, StatisticsSnapshot> statisticsSnapshotSortedMap) {
        this.statisticsSnapshotSortedMap = statisticsSnapshotSortedMap;
    }

    public Map.Entry<Long, StatisticsSnapshot> getLastSnapshotEntry(){

        if(this.statisticsSnapshotSortedMap.isEmpty()){
            return null;
        }

        return (Map.Entry<Long, StatisticsSnapshot>)this.statisticsSnapshotSortedMap.entrySet().toArray()[0];
    }

    public void addSnapShotEntry(long timestamp, StatisticsSnapshot statisticsSnapshot){
        this.statisticsSnapshotSortedMap.put(timestamp, statisticsSnapshot);
    }

    public SortedMap<Long, StatisticsSnapshot> getTempSnapShotSortedMap(){
        return new TreeMap<>(new TransactionTimeStampComparator());
    }

    public StatisticsSnapshot getOldestSnapshotInWindow(Long lookBackTime){
        int lookBackAsInt = Integer.parseInt(lookBackTime.toString());
        return this.getLastSnapshotEntry().getValue().getPreviousMinuteSnapshot().get(lookBackAsInt);
    }

    private class TransactionTimeStampComparator implements Comparator<Long> {

        @Override
        public int compare(Long a, Long b){
            if(a < b){
                return -1;
            } else if(a > b){
                return 1;
            }
            return 0;
        }

    }

}
