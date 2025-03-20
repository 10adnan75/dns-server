import java.io.IOException;
import java.net.DatagramPacket;

public interface IDNSMessage {

    public byte[] parse(DatagramPacket packet) throws IOException;

    default DatagramPacket createResponsePacket(byte[] data, DatagramPacket originalPacket) {

        return new DatagramPacket(data, data.length, originalPacket.getSocketAddress());

    }

}