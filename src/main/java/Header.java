import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public class Header implements DNSHeader {

    private short ID;
    private short FLAGS;
    private short QDCOUNT;
    private short ANCOUNT;
    private short NSCOUNT;
    private short ARCOUNT;

    public Header(short ID, short FLAGS, short QDCOUNT, short ANCOUNT, short NSCOUNT, short ARCOUNT) {

        this.ID = ID;
        this.FLAGS = FLAGS;
        this.QDCOUNT = QDCOUNT;
        this.ANCOUNT = ANCOUNT;
        this.NSCOUNT = NSCOUNT;
        this.ARCOUNT = ARCOUNT;

    }

    public void addHeader(ByteBuffer DNSMessage) {    

        DNSMessage.order(ByteOrder.BIG_ENDIAN);

        DNSMessage.putShort(this.ID);
        DNSMessage.putShort(this.FLAGS);
        DNSMessage.putShort(this.QDCOUNT);
        DNSMessage.putShort(this.ANCOUNT);
        DNSMessage.putShort(this.NSCOUNT);
        DNSMessage.putShort(this.ARCOUNT);

        return;

    }

}