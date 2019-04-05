package comms;

import java.util.Calendar;

/**
 *
 * @author cnmoro
 */
public class ClockTicker extends Thread {

    public ClockTicker() {
        CommonInfo.calendar = Calendar.getInstance();
    }

    @Override
    public void run() {
        try {
            while (true) {
                sleep();
                tick();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    void sleep() throws InterruptedException {
        Thread.sleep(500);
    }

    void tick() {
        CommonInfo.calendar.add(Calendar.MILLISECOND, 500);
    }
}
