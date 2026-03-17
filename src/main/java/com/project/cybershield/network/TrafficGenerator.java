package com.project.cybershield.network;

import com.project.cybershield.ids.IdsEngine;
import com.project.cybershield.ids.Packet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Random;


public class TrafficGenerator implements Runnable{
    private final Random rand = new Random();
    private static final Logger log = LoggerFactory.getLogger(TrafficGenerator.class);


    @Override
    public void run() {
        while(true){
            Packet p = generate();
            //IdsEngine.analyze(p);

            try {
                Thread.sleep(100);
            } catch (InterruptedException ex) {
                log.error("Error with current thread " + ex.getMessage());
                throw new RuntimeException(ex);
            }
        }
    }

    private Packet generate(){
        int r = rand.nextInt(100);

        //if(r < 80) return benign();
        //if(r < 90) return generateBruteforce();
        //return generatePortScan();
        return null;
    }
}
