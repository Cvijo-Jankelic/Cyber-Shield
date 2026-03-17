package com.project.cybershield.server;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;

public class UdpAlertServer {
    private static final int PORT = 9999;

    public static void main(String[] args) throws Exception {
        System.out.println("UDP Alert Server listening on port " + PORT);

        // Try-with-resources ensures the socket is automatically closed
        try (DatagramSocket socket = new DatagramSocket(PORT)) {

            byte[] buffer = new byte[4096];

            while (true) {
                DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
                socket.receive(packet);

                String received = new String(
                        packet.getData(), 0, packet.getLength(), StandardCharsets.UTF_8
                );

                System.out.println("ALERT RECEIVED:");
                System.out.println(received);

                // Simple acknowledgment response
                String response = "ACK: alert received at " + LocalDateTime.now();
                byte[] respBytes = response.getBytes(StandardCharsets.UTF_8);

                DatagramPacket resp = new DatagramPacket(
                        respBytes, respBytes.length,
                        packet.getAddress(), packet.getPort()
                );

                socket.send(resp);
            }

        } catch (Exception e) {
            System.err.println("Error in UDP Alert Server: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
