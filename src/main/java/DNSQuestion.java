import java.nio.ByteBuffer;

public interface DNSQuestion {

    public void addQuestion(ByteBuffer DNSMessage);
    
}
