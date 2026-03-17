package com.project.cybershield.host;

import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

// sluzi za drzanje hoststatea za svaki srcIp i brisanje starih hostova
public class HostTracker {
    private final Map<HostKey, HostState> hostStateMap = new ConcurrentHashMap<>();
    private final long idleTimeoutMillis;

    public HostTracker(long idleTimeoutMillis) {
        this.idleTimeoutMillis = idleTimeoutMillis;
    }

    public HostState getOrCreate(HostKey hostKey, long nowMillis){
        return hostStateMap.computeIfAbsent(hostKey, k -> new HostState(k, nowMillis));
    }

    public int cleanup(long nowMillis){
        int removed = 0;
        Iterator<Map.Entry<HostKey, HostState>> it = hostStateMap.entrySet().iterator();
        while(it.hasNext()){
            HostState state = it.next().getValue();
            if(nowMillis - state.lastSeenMillis() > idleTimeoutMillis){
                it.remove();
                removed++;
            }
        }
        return removed;
    }

    public int size() {
        return hostStateMap.size();
    }
}
