package com.project.cybershield.client;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.nio.charset.StandardCharsets;

public class UdpAlertClient {
    private final InetAddress serverAddr;
    private final int serverPort;
    private final DatagramSocket socket;

    public UdpAlertClient(String host, int port) throws Exception {
        this.serverAddr = InetAddress.getByName(host);
        this.serverPort = port;
        this.socket = new DatagramSocket();
    }

    public void sendAlert(
            String signatureId,
            String srcIp,
            int severity,
            String payloadText
    ) throws Exception {

        // STREAM PODATAKA (JSON tekst)
        String json = """
        {
          "signature": "%s",
          "sourceIp": "%s",
          "severity": %d,
          "payload": "%s",
          "timestamp": %d
        }
        """.formatted(signatureId, srcIp, severity,
                payloadText.replace("\"", "'"),
                System.currentTimeMillis());

        byte[] data = json.getBytes(StandardCharsets.UTF_8);

        DatagramPacket packet = new DatagramPacket(
                data, data.length, serverAddr, serverPort
        );

        socket.send(packet);

        // primitak odgovora
        byte[] respBuf = new byte[1024];
        DatagramPacket response = new DatagramPacket(respBuf, respBuf.length);
        socket.receive(response);

        String ack = new String(
                response.getData(), 0, response.getLength(), StandardCharsets.UTF_8
        );

        System.out.println("Server response: " + ack);
    }
}
