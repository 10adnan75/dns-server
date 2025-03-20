import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.net.DatagramPacket;
import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.Map;

public class Parser implements IDNSMessage {

    private Map<String, byte[]> dnsRecords = new HashMap<>();

    public Parser() {

        initializeDnsRecords();

    }

    private void initializeDnsRecords() {

        dnsRecords.put("10adnan75.github.io", new byte[] { (byte) 42, (byte) 42, (byte) 42, (byte) 42 });
        dnsRecords.put("localhost", new byte[] { 127, 0, 0, 1 });

    }

    public byte[] parse(DatagramPacket packet) throws IOException {

        ByteBuffer DNSMessage = ByteBuffer.allocate(512);
        DataInputStream dataInputStream = new DataInputStream(new ByteArrayInputStream(packet.getData()));

        short ID = dataInputStream.readShort();
        short LINE = dataInputStream.readShort();
        short OP_CODE = (short) ((LINE >> 11) & 0b1111);
        boolean RD = ((LINE >> 8) & 0b1) == 1;
        short R_CODE = OP_CODE != 0 ? (short) 4 : (short) 0;
        short FLAGS = (short) (0x8000 | (OP_CODE << 11) | (RD ? 0x0100 : 0) | R_CODE);
        short QD_COUNT = dataInputStream.readShort();
        short AN_COUNT = dataInputStream.readShort();
        short NS_COUNT = dataInputStream.readShort();
        short AR_COUNT = dataInputStream.readShort();

        StringBuilder domainName = new StringBuilder();
        int length;

        while ((length = dataInputStream.readByte() & 0xFF) > 0) {

            byte[] label = new byte[length];
            dataInputStream.readFully(label, 0, length);

            if (domainName.length() > 0) {

                domainName.append(".");

            }

            domainName.append(new String(label));

        }

        String NAME = domainName.toString();
        short TYPE = dataInputStream.readShort();
        short CLASS = dataInputStream.readShort();

        byte[] DATA = lookupIPAddress(NAME, TYPE);
        short RD_LENGTH = (short) (DATA.length);
        AN_COUNT = DATA != null ? (short) 0x01 : (short) 0x00;
        int TTL = 60;

        new Header(ID, FLAGS, QD_COUNT, AN_COUNT, NS_COUNT, AR_COUNT).addHeader(DNSMessage);

        new Question(NAME, TYPE, CLASS).addQuestion(DNSMessage);

        new Answer(NAME, TYPE, CLASS, TTL, RD_LENGTH, DATA).addAnswer(DNSMessage);

        return DNSMessage.array();

    }

    private byte[] lookupIPAddress(String domainName, short recordType) {

        return new byte[] { 0x08, 0x08, 0x08, 0x08 };

    }

    public void addDnsRecord(String domainName, byte[] ipAddress) {

        dnsRecords.put(domainName, ipAddress);

    }

    public void removeDnsRecord(String domainName) {

        dnsRecords.remove(domainName);

    }

}