package com.project.cybershield.host;

import java.util.HashSet;
import java.util.Set;

// Sluzi za agregaciju ponasanja jednog izvora(srcIp-a) u vremenskom prozoru
// Uzeli smo SYN bez ACK kao signal skeniranja
public class HostState {
    private final HostKey hostKey;
    private final Object stateLock = new Object();

    private long windowStartMillis;
    private long lastSeenMillis;

    private long synPacketsInWindow;
    private final Set<Integer> dstPortsInWindow = new HashSet<>();
    private long ackPacketsInWindow;

    //Alert kad smo zadnji put alertali za ovaj host
    private long lastAlertMillis;

    public HostState(HostKey hostKey, long nowMillis) {
        this.hostKey = hostKey;
        this.windowStartMillis = nowMillis;
        this.lastSeenMillis = nowMillis;
    }

    /**
     * Update windowed counters for this host.
     * - lastSeenMillis: koristi se za cleanup (idle removal)
     * - fixed window reset: ako je prozor istekao, resetiramo brojače i set portova
     * - broji samo SYN bez ACK (signal za scan)
     *
     *  SYNCHRONIZED UPDATE - thread-safe compound operation
     *  MECHANISM: synchronized block (monitor lock)
     *  Zašto: Više threadova mogu update-ati isti host (isti srcIp)
     *  Moramo atomično: check window + reset + update counters
     */
    public void update(long nowMillis, boolean isSynNoAck, boolean isAck, Integer dstPort, long windowMillis){

        //Thread ne ceka na nikog pa netrebamo obavjestavati druge threadovce, a thread se otpusta cim izadje i synchronized bloka
        //Samo stiti podatke od concurrent modifikacije
        synchronized(stateLock){
            this.lastSeenMillis = nowMillis;
            // fixed window: reset kada prodje windowMillis
            if(nowMillis - windowStartMillis > windowMillis){
                windowStartMillis = nowMillis;
                synPacketsInWindow = 0;
                dstPortsInWindow.clear();
                ackPacketsInWindow = 0;
            }

            if(isSynNoAck){
                synPacketsInWindow++;
                if(dstPort != null) dstPortsInWindow.add(dstPort);
            }

            if(isAck){
                ackPacketsInWindow++;
            }
        }

    }
    public HostKey key() { return hostKey; }
    public synchronized long lastSeenMillis() { return lastSeenMillis; }
    public synchronized long synPacketsInWindow() { return synPacketsInWindow; }
    public synchronized int uniqueDstPortsInWindow() { return dstPortsInWindow.size(); }
    public Set<Integer> dstPortsInWindowView() { return Set.copyOf(dstPortsInWindow); }


    public long lastAlertMillis() { return lastAlertMillis; }
    public synchronized void markAlerted(long nowMillis) { this.lastAlertMillis = nowMillis; }

    public synchronized long ackPacketsInWindow() {
        return ackPacketsInWindow;
    }
}
