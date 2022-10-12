package LayeredBFS;

import java.util.concurrent.*;

// DelayObject class for delaying message by the specified time units
public class DelayObject implements Delayed {

    private int p_uid;
    // private int c_uid;
    private int reqVal;
    private String messageType;
    private long delayTime;
    private long time;
    private static final int timeFactor = 10;

    public DelayObject(int p_uid, int reqVal, String messageType, long delayTime) {
        this.p_uid = p_uid;
        // this.c_uid = c_uid;
        this.reqVal = reqVal;
        this.messageType = messageType;
        this.delayTime = delayTime;
        this.time = System.currentTimeMillis() + timeFactor * delayTime;
    }

    // Implementing getDelay() method of Delayed
    @Override
    public long getDelay(TimeUnit unit) {
        long diff = time - System.currentTimeMillis();
        return unit.convert(diff, TimeUnit.MILLISECONDS);
    }

    // Implementing compareTo() method of Delayed
    @Override
    public int compareTo(Delayed o) {
        if (this.time < ((DelayObject) o).time) {
            return -1;
        }
        if (this.time > ((DelayObject) o).time) {
            return 1;
        }
        return 0;
    }

    public int getPUid() {
        return this.p_uid;
    }

    // public int getCUid() {
    //     return this.c_Uid; 
    // }

    public int getReqVal(){
        return this.reqVal;
    }

    public String getMessageType() {
        return this.messageType;
    }
}
