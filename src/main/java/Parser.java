import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.net.DatagramPacket;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;

public class Parser {

    public byte[] parse(DatagramPacket packet) throws IOException {
        final ByteBuffer DNSMessage = ByteBuffer.allocate(512);
        final DataInputStream dataInputStream = new DataInputStream(new ByteArrayInputStream(packet.getData()));

        final short ID = dataInputStream.readShort();
        final short LINE = dataInputStream.readShort();
        final short OP_CODE = (short) ((LINE >> 11) & 0b1111);
        final boolean RD = ((LINE >> 8) & 0b1) == 1;
        final short R_CODE = OP_CODE != 0 ? (short) 4 : (short) 0;
        final short FLAGS = (short) (0x8000 | (OP_CODE << 11) | (RD ? 0x0100 : 0) | R_CODE);
        final short QD_COUNT = dataInputStream.readShort();
        final short AN_COUNT = dataInputStream.readShort();
        final short NS_COUNT = dataInputStream.readShort();
        final short AR_COUNT = dataInputStream.readShort();

        StringBuilder domainName = new StringBuilder();
        int length;

        while ((length = dataInputStream.readByte() & 0xFF) > 0) {
            byte[] label = new byte[length];
            dataInputStream.readFully(label, 0, length);
            if (domainName.length() > 0) {
                domainName.append(".");
            }
            domainName.append(new String(label, StandardCharsets.UTF_8));
        }

        String NAME = domainName.toString();

        short Q_TYPE = dataInputStream.readShort();
        short Q_CLASS = dataInputStream.readShort();

        new Header(ID, FLAGS, QD_COUNT, AN_COUNT, NS_COUNT, AR_COUNT).addHeader(DNSMessage);

        new Question(NAME, Q_TYPE, Q_CLASS).addQuestion(DNSMessage);

        new Answer(NAME, (short) 0x01, (short) 0x01, 120, (short) 4,
                new byte[] { (byte) 255, (byte) 255, (byte) 255, (byte) 255 }).addAnswer(DNSMessage);

        return DNSMessage.array();
    }

}
