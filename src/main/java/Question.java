import java.nio.ByteBuffer;

public class Question extends EncodeDomainName implements DNSQuestion {

    private String NAME;
    private short Q_TYPE;
    private short Q_CLASS;

    public Question(String NAME, short Q_TYPE, short Q_CLASS) {

        this.NAME = NAME;
        this.Q_TYPE = Q_TYPE;
        this.Q_CLASS = Q_CLASS;

    }

    public void addQuestion(ByteBuffer DNSMessage) {

        DNSMessage.put(encodeDomainName(this.NAME));
        DNSMessage.putShort(this.Q_TYPE);
        DNSMessage.putShort(this.Q_CLASS);

        return;

    }

}