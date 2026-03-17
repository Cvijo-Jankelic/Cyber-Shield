package com.project.cybershield.network;

import com.project.cybershield.enums.PacketType;
import com.project.cybershield.ids.Packet;

import java.time.LocalDateTime;
import java.util.Random;

public class MaliciousGenerator {
    private final Random rnd = new Random();

    private final String[] maliciousPayloads = {
            "unauthorized_ip:10.5.8.12",
            "unauthorized_ip:125.70.19.244",
            "high_udp_rate",
            "failed_login_attempts",
            "multiple_ports:1-1024",
            "bruteforce_ssh",
            "port_scan_detected",
            "udp_flood_attack"
    };


    public String randomIP(){
        return "192.13." + rnd.nextInt(255) + "." + rnd.nextInt(255);
    }

    public int randomPort(){
        return 1000 + rnd.nextInt(60000);
    }

}
