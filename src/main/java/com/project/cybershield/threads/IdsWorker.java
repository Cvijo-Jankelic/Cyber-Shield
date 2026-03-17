package com.project.cybershield.threads;

import com.project.cybershield.decode.DecodedPacket;
import com.project.cybershield.decode.PacketDecoder;
import com.project.cybershield.ids.IdsEngine;

import java.util.concurrent.BlockingQueue;

public class IdsWorker implements Runnable{
    private final BlockingQueue<DecodedPacket> queue;
    private final IdsEngine idsEngine;


    public IdsWorker(BlockingQueue<DecodedPacket> queue, IdsEngine idsEngine) {
        this.queue = queue;
        this.idsEngine = idsEngine;
    }


    @Override
    public void run() {
        try{
            while(true){
                DecodedPacket dp = queue.take();
                //
            }
        } catch (InterruptedException e) {
            System.out.println("Error with current thread in ids worker later add logging");
            Thread.currentThread().interrupt();
            throw new RuntimeException(e);
        }
    }
}
