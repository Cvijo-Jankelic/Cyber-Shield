package com.project.cybershield.ids;

import com.project.cybershield.config.IdsConfig;
import com.project.cybershield.config.JsonSignatureLoader;
import com.project.cybershield.decode.DecodedPacket;
import com.project.cybershield.decode.PacketDecoder;
import com.project.cybershield.detect.PortScanDetector;
import com.project.cybershield.detect.SynFloodDetector;
import com.project.cybershield.flow.FlowKey;
import com.project.cybershield.flow.FlowKeysGen;
import com.project.cybershield.flow.FlowState;
import com.project.cybershield.flow.FlowTable;
import com.project.cybershield.host.HostKey;
import com.project.cybershield.host.HostState;
import com.project.cybershield.host.HostTracker;
import com.project.cybershield.network.PacketSource;
import com.project.cybershield.repository.IncidentRepo;
import com.project.cybershield.test.PcapOfflineSource;
import org.pcap4j.packet.Packet;

import java.nio.file.Path;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.*;


public class IdsEngine {

    // --- Existing fields you already have ---
    private final List<SignaturePayload> signatures; // not used yet in this temporary engine
    private final IdsConfig config;
    private final IncidentRepo incidentRepo = new IncidentRepo();

    //Concurrency mechanism
    private final ExecutorService processingPool;
    private final BlockingQueue<Packet> packetQueue;

    // --- Runtime components (temporary) ---
    private final PacketSource packetSource;
    private final PacketDecoder decoder = new PacketDecoder();
    private final SignatureEngine signatureEngine = new SignatureEngine(JsonSignatureLoader.load());

    private final FlowTable flowTable = new FlowTable(60_000);      // 60s idle timeout
    private final HostTracker hostTracker = new HostTracker(120_000);
    // Test-friendly konfig, kasnije cemo izvuci u idsConfigKlasu

    private final SynFloodDetector synFloodDetector =
            new SynFloodDetector(
                    10_000, // window 10s
                    1,     // synThreshold (spusti za test)
                    0,      // ackMax
                    8.0,    // syn/ack ratio
                    0  // cooldown
            );

    // Port scan tuning (temporary):
    // window 10s, uniquePorts>=20, syn>=30, cooldown 10s
    private final PortScanDetector portScanDetector =
            new PortScanDetector(10_000, 20, 30, 10_000);

    public IdsEngine(List<SignaturePayload> signatures, IdsConfig config, PacketSource packetSource) {
        this.signatures = signatures;
        this.config = config;
        this.packetSource = Objects.requireNonNull(packetSource, "packetSource");

        this.processingPool = Executors.newFixedThreadPool(4);
        this.packetQueue = new LinkedBlockingQueue<>(10000);
    }

    /**
     * TEMP RUNNER:
     * - reads packets (offline or live)
     * - decodes L2->L4
     * - updates flow table and host tracker
     * - prints stats and port-scan alerts
     */
    public void runDebug(long maxPackets) throws Exception {

        long seen = 0;
        long ip = 0, tcp = 0, udp = 0, icmp = 0, other = 0;

        System.out.println("[IDS] Starting packet source...");
        packetSource.start();


        System.out.println("[IDS] Source started.");

        try {
            while (packetSource.isOpen()) {

                // Live mode may return null on timeout

                //Test za pcaphandle
                Packet raw = packetSource.nextPacket();
                if (raw == null) {
                    continue;
                }
                seen++;

                DecodedPacket dp = decoder.decode(raw, null);

                // protocol stats
                if (!dp.isIp()) {
                    other++;
                    if (other <= 5) {
                        System.out.println("[OTHER] raw=" + raw);
                    }
                    continue;
                }

                var hits = signatureEngine.matchAll(dp);
                for (var sig : hits) {
                    System.out.println("[ALERT][SIGNATURE] " + sig.getId() + " - " + sig.getName()
                            + " src=" + dp.srcIp() + ":" + dp.srcPort()
                            + " -> " + dp.dstIp() + ":" + dp.dstPort());
                }

                ip++;

                switch (dp.l4Proto()) {
                    case TCP -> tcp++;
                    case UDP -> udp++;
                    case ICMP -> icmp++;
                    default -> other++;
                }

                long nowMillis = dp.timestampMillis();

                // --- FLOW TRACKING ---
                FlowKey fk = FlowKeysGen.from(dp);
                FlowState fs = flowTable.getOrCreate(fk, nowMillis);
                // Ako ti FlowState.update trenutno prima (now, payloadLen) a ne (now, payloadLen, dp),
                // prilagodi ovaj poziv.
                fs.update(nowMillis, dp.payload().length, dp);

                // --- PORT SCAN (HOST-BASED) ---
                if (dp.isTcp()) {
                    boolean synNoAck = dp.tcpFlags().syn() && !dp.tcpFlags().ack();
                    boolean ackOnlyOrAnyAck = dp.tcpFlags().ack();

                    HostState hs = hostTracker.getOrCreate(new HostKey(dp.srcIp()), nowMillis);
                    hs.update(nowMillis, synNoAck, ackOnlyOrAnyAck, dp.dstPort(), portScanDetector.windowMillis());
                    System.out.println(
                            "[DBG][TCP] src=" + dp.srcIp()
                                    + " synNoAck=" + hs.synPacketsInWindow()
                                    + " ack=" + hs.ackPacketsInWindow()
                    );


                    if (!synNoAck) {
                        if (portScanDetector.isPortScan(hs) && portScanDetector.canAlertNow(hs, nowMillis)) {
                            hs.markAlerted(nowMillis);
                            System.out.println("[ALERT][PORT_SCAN] src=" + dp.srcIp()
                                    + " uniquePorts=" + hs.uniqueDstPortsInWindow()
                                    + " syn=" + hs.synPacketsInWindow());
                            }

                        }

                    if (synFloodDetector.isSynFloodLike(hs) && synFloodDetector.canAlertNow(hs, nowMillis)) {
                        hs.markAlerted(nowMillis);

                        System.out.println("[ALERT][SYN_FLOOD] src=" + dp.srcIp()
                                + " synNoAck=" + hs.synPacketsInWindow()
                                + " ack=" + hs.ackPacketsInWindow());
                    }

                }

                // periodic debug print
                if (seen % 2000 == 0) {
                    int removedFlows = flowTable.cleanup(nowMillis);
                    int removedHosts = hostTracker.cleanup(nowMillis);

                    System.out.println("[STATS] seen=" + seen
                            + " ip=" + ip + " tcp=" + tcp + " udp=" + udp + " icmp=" + icmp + " other=" + other
                            + " flows=" + flowTable.size() + " hosts=" + hostTracker.size()
                            + " cleanup(flows=" + removedFlows + ",hosts=" + removedHosts + ")");
                }

                if (maxPackets > 0 && seen >= maxPackets) {
                    System.out.println("[IDS] Max packets reached: " + maxPackets);
                    break;
                }
            }
        } finally {
            packetSource.close();
            System.out.println("[IDS] Source closed.");
        }

        System.out.println("[FINAL] seen=" + seen
                + " ip=" + ip + " tcp=" + tcp + " udp=" + udp + " icmp=" + icmp + " other=" + other
                + " flows=" + flowTable.size() + " hosts=" + hostTracker.size());
    }

    // ---------------------------
    // Convenience factory methods
    // ---------------------------

    /** Quick offline runner. */
    public static IdsEngine offline(List<SignaturePayload> signatures, IdsConfig config, Path pcapFile) {
        PacketSource src = new PcapOfflineSource(pcapFile);
        return new IdsEngine(signatures, config, src);
    }

    /** Quick live runner. */

    public static IdsEngine online(List<SignaturePayload> signatures, IdsConfig config, String networkInterface ) {
       // PacketSource src = new PcapLiveSource(networkInterface);
        //return new IdsEngine(signatures, config, )

        return null;
    }

}
