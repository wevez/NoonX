package tech.mania.core.util.legit;

import java.util.concurrent.ThreadLocalRandom;

public class LegitCPSTimer {

    private int remainder = 0;
    private long time = System.currentTimeMillis();

    public int getClicks(double targetedCPS) {
        if (ThreadLocalRandom.current().nextDouble(1.3) > Math.sqrt(remainder + 4.0) / 3.0) { // choke (something around remainder = 9 forces it impossible)
            remainder++;
            return 0;
        };
        int ticks = (int) Math.round((System.currentTimeMillis() - time) / (1000.0 / (targetedCPS + remainder)));
        time += (long) (ticks * (1000.0 / (targetedCPS + remainder)));
        remainder = 0;
        return ticks;
    }

    public void reset(double targetedCPS) {
        time = (long) (System.currentTimeMillis() + (1000.0 / targetedCPS)); // The idea is that, it resets it not to 0 clicks but to 1 click means it will get the first hit
        remainder = 0;
    }
}
