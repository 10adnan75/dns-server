import java.nio.ByteBuffer;

public class Answer extends EncodeDomainName implements DNSAnswer {

    private String NAME;
    private short A_TYPE;
    private short A_CLASS;
    private int TTL;
    private short LENGTH;
    private byte[] DATA;

    public Answer(String NAME, short A_TYPE, short A_CLASS, int TTL, short LENGTH, byte[] DATA) {

        this.NAME = NAME;
        this.A_TYPE = A_TYPE;
        this.A_CLASS = A_CLASS;
        this.TTL = TTL;
        this.LENGTH = LENGTH;
        this.DATA = DATA;

    }

    public void addAnswer(ByteBuffer DNSMessage) {

        DNSMessage.put(encodeDomainName(this.NAME));
        DNSMessage.putShort(this.A_TYPE);
        DNSMessage.putShort(this.A_CLASS);
        DNSMessage.putInt(this.TTL);
        DNSMessage.putShort(this.LENGTH);
        DNSMessage.put(this.DATA);

        return;

    }

}
