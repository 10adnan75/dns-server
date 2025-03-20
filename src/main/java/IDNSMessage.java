import java.io.IOException;
import java.net.DatagramPacket;

public interface IDNSMessage {

    public byte[] parse(DatagramPacket packet) throws IOException;

}