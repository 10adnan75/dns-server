import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;

public class DNSSection {

    private static int OPCODE = 15 << 11;
    private static int RD = 1 << 8;
    private static int RCODE = 1 << 2;
    
    public static Header parseHeader(byte[] receivedMessage) throws IOException {

        DataInputStream dataInputStream = new DataInputStream(new ByteArrayInputStream(receivedMessage));

        short ID = dataInputStream.readShort();
        int LINE = dataInputStream.readShort();
        int FLAGS = 1 << 15;

        FLAGS = setBits(FLAGS, LINE & OPCODE);
        FLAGS = setBits(FLAGS, LINE & RD);
        FLAGS = setBits(FLAGS, RCODE);

        return new Header(ID, (short)FLAGS, (short)0x01, (short)0x01, (short)0x00, (short)0x00);

    }

    private static int setBits(int integer, int mask) {

        return integer | mask;
        
    }

}
