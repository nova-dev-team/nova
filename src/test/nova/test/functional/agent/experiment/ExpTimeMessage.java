package nova.test.functional.agent.experiment;

public class ExpTimeMessage {
    public ExpTimeMessage() {

    }

    public String timeType = null;
    public long currentTime = 0;
    public String ip = null;

    public ExpTimeMessage(String ip, String tType, long curTime) {
        this.ip = ip;
        this.timeType = tType;
        this.currentTime = curTime;
    }

}
