import java.io.IOException;
import java.nio.ByteBuffer;

public interface DNSHeader {

    public void addHeader(ByteBuffer DNSMessage);

    default byte[] getHeaderBytes() throws IOException {

        ByteBuffer buffer = ByteBuffer.allocate(12);
        addHeader(buffer);
        byte[] bytes = new byte[buffer.position()];
        buffer.flip();
        buffer.get(bytes);

        return bytes;

    }

}