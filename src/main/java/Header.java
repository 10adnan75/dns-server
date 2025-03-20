import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public class Header implements DNSHeader {

    private short ID;
    private short FLAGS;
    private short QD_COUNT;
    private short AN_COUNT;
    private short NS_COUNT;
    private short AR_COUNT;

    public Header(short ID, short FLAGS, short QD_COUNT, short AN_COUNT, short NS_COUNT, short AR_COUNT) {

        this.ID = ID;
        this.FLAGS = FLAGS;
        this.QD_COUNT = QD_COUNT;
        this.AN_COUNT = AN_COUNT;
        this.NS_COUNT = NS_COUNT;
        this.AR_COUNT = AR_COUNT;

    }

    public void addHeader(ByteBuffer DNSMessage) {

        DNSMessage.order(ByteOrder.BIG_ENDIAN);

        DNSMessage.putShort(this.ID);
        DNSMessage.putShort(this.FLAGS);
        DNSMessage.putShort(this.QD_COUNT);
        DNSMessage.putShort(this.AN_COUNT);
        DNSMessage.putShort(this.NS_COUNT);
        DNSMessage.putShort(this.AR_COUNT);

        return;

    }

}