package com.project.cybershield.flow;

import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantReadWriteLock;


public class FlowTable {
    private final Map<FlowKey, FlowState> flowStateMap = new ConcurrentHashMap<>();
    private final long idleTimeoutMillis;


    public FlowTable(long idleTimeoutMillis) {
        this.idleTimeoutMillis = idleTimeoutMillis;
    }

    public FlowState getOrCreate(FlowKey flowKey, long nowMillis){
        return  flowStateMap.computeIfAbsent(flowKey, fk -> new FlowState(fk, nowMillis));

    }

    public int size(){
        return flowStateMap.size();
    }


    /**
     * Remove flows that were idle longer than idleTimeoutMillis.
     * Call periodically (e.g. every N packets or every second).
     */
    public int cleanup(long nowMillis){
        int removed = 0;
        Iterator<Map.Entry<FlowKey, FlowState>> iterator = flowStateMap.entrySet().iterator();
        while(iterator.hasNext()){
            Map.Entry<FlowKey, FlowState> e = iterator.next();
            FlowState st = e.getValue();
            if(nowMillis - st.getLastSeenMillis() > idleTimeoutMillis){
                iterator.remove();
                removed++;
            }
        }
        return removed;
    }

}
