import java.nio.ByteBuffer;

public class Answer extends Encode implements DNSAnswer {

    private String name;
    private short aType;
    private short aClass;
    private int ttl;
    private short length;
    private byte[] data;
    
    public Answer(String name, short aType, short aClass, int ttl, short length, byte[] data) {

        this.name = name;
        this.aType = aType;
        this.aClass = aClass;
        this.ttl = ttl;
        this.length = length;
        this.data = data;

    }

    public void addAnswer(ByteBuffer DNSMessage) {

        DNSMessage.put(encodeDomainName(this.name));
        DNSMessage.putShort(this.aType);
        DNSMessage.putShort(this.aClass);
        DNSMessage.putInt(this.ttl);
        DNSMessage.putShort(this.length);
        DNSMessage.put(this.data);

        return;

    }

}
