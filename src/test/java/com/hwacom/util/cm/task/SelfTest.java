package com.hwacom.util.cm.task;

import java.util.Calendar;

public class SelfTest {

    public static void main(String[] args) {
        /*
        Calendar c = Calendar.getInstance();
        String s = String
                .format("show running-config | file disk%1$d:%2$srunning-config-%3$tY-%3$tm-%3$te.cfg location %4$d/RSP%5$d/CPU0",
                        1, "admin-", c, 0, 1);
        System.out.println(s);
        
        //scp disk1:/admin-running-config-2015-01-19.cfg feng@172.16.133.1:/Users/feng/Documents/HwaCom/admin-running-config-2015-01-19.cfg
        String a = String
                .format("scp disk%1$d:/%2$srunning-config-%3$tY-%3$tm-%3$te.cfg location %4$d/RSP%5$d/CPU0 %6$s@%7$s:%8$s%2$srunning-config-%3$tY-%3$tm-%3$te.cfg",
                        1, "admin-", c, 0, 1, "feng", "172.16.133.1", "/Users/feng/Documents/HwaCom/");
        System.out.println(a);
        */
        
        RunnableDemo R1 = new RunnableDemo( "Thread-1");
        R1.start();
        RunnableDemo R2 = new RunnableDemo( "Thread-2");
        R2.start();
    }

}

class RunnableDemo implements Runnable {
    private Thread t;
    private String threadName;

    RunnableDemo(String name) {
        threadName = name;
        System.out.println("Creating " + threadName);
    }

    public void run() {
        System.out.println("Running " + threadName);
        try {
            for (int i = 4; i > 0; i--) {
                System.out.println("Thread: " + threadName + ", " + i);
                // Let the thread sleep for a while.
                Thread.sleep(50);
            }
        } catch (InterruptedException e) {
            System.out.println("Thread " + threadName + " interrupted.");
        }
        System.out.println("Thread " + threadName + " exiting.");
    }

    public void start() {
        System.out.println("Starting " + threadName);
        if (t == null) {
            t = new Thread(this, threadName);
            t.start();
        }
    }

}
