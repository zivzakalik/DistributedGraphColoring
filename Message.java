import java.io.Serializable;
import java.math.BigInteger;

public class Message implements Serializable {
    private final int sourceID;
    private final int roundNum;
    private String c_v;
    private BigInteger endColor;

    private boolean undecided;

    public Message(int sourceID, int roundNum, String c_v) {
        this.c_v = c_v;
        this.roundNum = roundNum;
        this.sourceID = sourceID;
    }

    public Message(int sourceID, int roundNum, BigInteger endColor, boolean undecided) {
        this.sourceID = sourceID;
        this.endColor = endColor;
        this.roundNum = roundNum;
        this.undecided = undecided;
    }

    public int getSourceID() {
        return sourceID;
    }

    public int getRoundNum() {
        return roundNum;
    }

    public String getC_v() {
        return c_v;
    }

    public BigInteger getEndColor() {
        return endColor;
    }

    public boolean isUndecided() {
        return undecided;
    }
}
