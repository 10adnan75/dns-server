import java.nio.ByteBuffer;

public class Question extends Encode implements DNSQuestion {

    private String name;
    private short qType;
    private short qClass;

    public Question(String name, short qType, short qClass) {

        this.name = name;
        this.qType = qType;
        this.qClass = qClass;

    }

    public void addQuestion(ByteBuffer DNSMessage) {

        DNSMessage.put(encodeDomainName(this.name));
        DNSMessage.putShort(this.qType);
        DNSMessage.putShort(this.qClass);

        return;

    }

}