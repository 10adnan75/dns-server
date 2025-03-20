import java.io.IOException;
import java.nio.ByteBuffer;

public interface DNSAnswer {

    public void addAnswer(ByteBuffer DNSMessage);

    default byte[] getAnswerBytes() throws IOException {

        ByteBuffer buffer = ByteBuffer.allocate(512);
        addAnswer(buffer);
        byte[] bytes = new byte[buffer.position()];
        buffer.flip();
        buffer.get(bytes);

        return bytes;

    }

}