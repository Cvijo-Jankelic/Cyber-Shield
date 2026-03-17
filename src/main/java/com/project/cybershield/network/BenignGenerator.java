package com.project.cybershield.network;

import com.project.cybershield.enums.PacketType;
import com.project.cybershield.ids.Packet;

import java.time.LocalDateTime;
import java.util.Random;

public class BenignGenerator {
    private Random rnd = new Random();

    private final String[] normalPayloads = {
            "GET /index HTTP/1.1",
            "DNS lookup google.com",
            "TCP handshake",
            "Ping request",
            "HTTP/1.1 200 OK",
            "User login success",
            "Keep-alive",
            "TLS handshake",
            "Normal traffic"
    };

    public Packet next(){
        return new Packet(
                randomIp(),
                randomPort(),
                randomProtocol(),
                LocalDateTime.now(),
                randomPayload()
        );
    }

    private String randomPayload(){
        return normalPayloads[rnd.nextInt(normalPayloads.length)];
    }

    private String randomIp(){
        return "192.168." + rnd.nextInt(255) + "." + rnd.nextInt(255);
    }

    private int randomPort(){
        return 1000 + rnd.nextInt(9000);
    }
    private String randomProtocol(){
        return rnd.nextBoolean() ? "TCP" : "UDP";
    }

}
