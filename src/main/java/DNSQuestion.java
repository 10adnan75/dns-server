import java.io.IOException;
import java.nio.ByteBuffer;

public interface DNSQuestion {

    public void addQuestion(ByteBuffer DNSMessage);

    default byte[] getQuestionBytes() throws IOException {

        ByteBuffer buffer = ByteBuffer.allocate(512);
        addQuestion(buffer);
        byte[] bytes = new byte[buffer.position()];
        buffer.flip();
        buffer.get(bytes);

        return bytes;

    }

}