import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.net.DatagramPacket;
import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.Map;
import java.util.ArrayList;
import java.util.List;

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
        byte[] data = packet.getData();
        int packetLength = packet.getLength();
        DataInputStream dataInputStream = new DataInputStream(new ByteArrayInputStream(data, 0, packetLength));

        short ID = dataInputStream.readShort();
        short FLAGS = dataInputStream.readShort();
        short QD_COUNT = dataInputStream.readShort();
        short AN_COUNT = (short) (dataInputStream.readShort() | QD_COUNT);
        short NS_COUNT = dataInputStream.readShort();
        short AR_COUNT = dataInputStream.readShort();
        short OP_CODE = (short) ((FLAGS >> 11) & 0xF);

        FLAGS = (short) (FLAGS | 0x8000);

        if (OP_CODE != 0) {

            FLAGS = (short) ((FLAGS & 0xFFF0) | 0x0004);

        }

        List<String> domainNames = new ArrayList<>();
        List<Short> types = new ArrayList<>();
        List<Short> classes = new ArrayList<>();

        int headerSize = 12;
        for (int i = 0; i < QD_COUNT; i++) {

            String domainName = readDomainName(data, dataInputStream, headerSize);
            short TYPE = dataInputStream.readShort();
            short CLASS = dataInputStream.readShort();

            domainNames.add(domainName);
            types.add(TYPE);
            classes.add(CLASS);

        }

        new Header(ID, FLAGS, QD_COUNT, AN_COUNT, NS_COUNT, AR_COUNT).addHeader(DNSMessage);

        for (int i = 0; i < domainNames.size(); i++) {

            new Question(domainNames.get(i), types.get(i), classes.get(i)).addQuestion(DNSMessage);

        }

        for (int i = 0; i < domainNames.size(); i++) {

            byte[] ipAddress = generateIPAddress(domainNames.get(i));
            new Answer(
                    domainNames.get(i),
                    types.get(i),
                    classes.get(i),
                    60,
                    (short) 4,
                    ipAddress).addAnswer(DNSMessage);

        }

        byte[] response = new byte[DNSMessage.position()];
        DNSMessage.flip();
        DNSMessage.get(response);

        return response;

    }

    private String readDomainName(byte[] data, DataInputStream input, int baseOffset) throws IOException {

        StringBuilder domainName = new StringBuilder();
        boolean jumped = false;
        int savedPosition = 0;

        while (true) {

            int length = input.readUnsignedByte();

            if ((length & 0xC0) == 0xC0) {

                if (!jumped) {

                    savedPosition = input.available();
                    jumped = true;

                }

                int offset = ((length & 0x3F) << 8) | (input.readUnsignedByte() & 0xFF);
                ByteArrayInputStream tempStream = new ByteArrayInputStream(data, offset, data.length - offset);
                input = new DataInputStream(tempStream);
                continue;

            }

            if (length == 0) {

                break;

            }

            if (domainName.length() > 0) {

                domainName.append(".");

            }

            byte[] label = new byte[length];
            input.readFully(label);
            domainName.append(new String(label));

        }

        if (jumped) {

            ByteArrayInputStream restoreStream = new ByteArrayInputStream(
                    data,
                    data.length - savedPosition,
                    savedPosition);
            input = new DataInputStream(restoreStream);

        }

        return domainName.toString();

    }

    private byte[] generateIPAddress(String domainName) {

        int hash = Math.abs(domainName.hashCode());

        return new byte[] {
                (byte) 192,
                (byte) 168,
                (byte) (hash % 256),
                (byte) ((hash >> 8) % 256)
        };

    }

    public void addDnsRecord(String domainName, byte[] ipAddress) {

        dnsRecords.put(domainName, ipAddress);

    }

    public void removeDnsRecord(String domainName) {

        dnsRecords.remove(domainName);

    }

}