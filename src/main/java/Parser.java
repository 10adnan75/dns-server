import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Parser implements IDNSMessage {

    private String resolverIp;
    private int resolverPort;
    private Map<String, byte[]> dnsRecords = new HashMap<>();

    public Parser() {

        initializeDnsRecords();

    }

    public Parser(String resolverIp, int resolverPort) {

        this.resolverIp = resolverIp;
        this.resolverPort = resolverPort;
        initializeDnsRecords();

    }

    private void initializeDnsRecords() {

        dnsRecords.put("10adnan75.github.io", new byte[] { (byte) 42, (byte) 42, (byte) 42, (byte) 42 });
        dnsRecords.put("localhost", new byte[] { 127, 0, 0, 1 });

    }

    public byte[] parse(DatagramPacket packet) throws IOException {

        byte[] data = packet.getData();
        int packetLength = packet.getLength();
        DataInputStream dataInputStream = new DataInputStream(new ByteArrayInputStream(data, 0, packetLength));

        short ID = dataInputStream.readShort();
        short FLAGS = dataInputStream.readShort();
        short QD_COUNT = dataInputStream.readShort();
        dataInputStream.readShort();
        dataInputStream.readShort();
        dataInputStream.readShort();

        if (resolverIp != null && resolverPort > 0) {

            if (QD_COUNT > 1) {

                return handleMultipleQueries(data, packetLength);

            } else {

                return forwardQuery(data, packetLength);

            }
        } else {

            ByteBuffer DNSMessage = ByteBuffer.allocate(512);
            FLAGS = (short) (FLAGS | 0x8000);
            short OP_CODE = (short) ((FLAGS >> 11) & 0xF);

            if (OP_CODE != 0) {

                FLAGS = (short) ((FLAGS & 0xFFF0) | 0x0004);

            }

            List<String> domainNames = new ArrayList<>();
            List<Short> types = new ArrayList<>();
            List<Short> classes = new ArrayList<>();

            int headerSize = 12;
            dataInputStream = new DataInputStream(new ByteArrayInputStream(data, 0, packetLength));
            dataInputStream.readShort();
            dataInputStream.readShort();
            dataInputStream.readShort();
            dataInputStream.readShort();
            dataInputStream.readShort();
            dataInputStream.readShort();

            for (int i = 0; i < QD_COUNT; i++) {

                String domainName = readDomainName(data, dataInputStream, headerSize);
                short TYPE = dataInputStream.readShort();
                short CLASS = dataInputStream.readShort();

                domainNames.add(domainName);
                types.add(TYPE);
                classes.add(CLASS);

            }

            new Header(ID, FLAGS, QD_COUNT, QD_COUNT, (short) 0, (short) 0).addHeader(DNSMessage);

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

    }

    private byte[] forwardQuery(byte[] data, int length) throws IOException {

        try (DatagramSocket socket = new DatagramSocket()) {

            DatagramPacket forwardPacket = new DatagramPacket(
                    data, length,
                    InetAddress.getByName(resolverIp),
                    resolverPort);

            socket.send(forwardPacket);

            byte[] responseBuffer = new byte[512];
            DatagramPacket responsePacket = new DatagramPacket(responseBuffer, responseBuffer.length);
            socket.receive(responsePacket);

            byte[] response = new byte[responsePacket.getLength()];
            System.arraycopy(responsePacket.getData(), 0, response, 0, responsePacket.getLength());

            return response;

        }

    }

    private byte[] handleMultipleQueries(byte[] data, int packetLength) throws IOException {

        DataInputStream dataInputStream = new DataInputStream(new ByteArrayInputStream(data, 0, packetLength));

        short ID = dataInputStream.readShort();
        short FLAGS = dataInputStream.readShort();
        short QD_COUNT = dataInputStream.readShort();
        dataInputStream.readShort();
        dataInputStream.readShort();
        dataInputStream.readShort();

        List<String> domainNames = new ArrayList<>();
        List<Short> types = new ArrayList<>();
        List<Short> classes = new ArrayList<>();
        List<byte[]> encodedNames = new ArrayList<>();

        for (int i = 0; i < QD_COUNT; i++) {

            int currentPosition = 12;

            for (int j = 0; j < i; j++) {

                currentPosition += encodedNames.get(j).length + 4;

            }

            String domainName = readDomainName(data, dataInputStream, currentPosition);
            byte[] encodedName = new EncodeDomainName().encodeDomainName(domainName);
            encodedNames.add(encodedName);

            short TYPE = dataInputStream.readShort();
            short CLASS = dataInputStream.readShort();

            domainNames.add(domainName);
            types.add(TYPE);
            classes.add(CLASS);

        }

        ByteBuffer combinedResponse = ByteBuffer.allocate(512);
        new Header(ID, (short) (FLAGS | 0x8000), QD_COUNT, QD_COUNT, (short) 0, (short) 0).addHeader(combinedResponse);

        for (int i = 0; i < domainNames.size(); i++) {

            new Question(domainNames.get(i), types.get(i), classes.get(i)).addQuestion(combinedResponse);

        }

        for (int i = 0; i < domainNames.size(); i++) {

            ByteBuffer singleQuery = ByteBuffer.allocate(512);
            new Header(ID, FLAGS, (short) 1, (short) 0, (short) 0, (short) 0).addHeader(singleQuery);
            new Question(domainNames.get(i), types.get(i), classes.get(i)).addQuestion(singleQuery);

            byte[] queryBytes = new byte[singleQuery.position()];
            singleQuery.flip();
            singleQuery.get(queryBytes);

            byte[] singleResponse = forwardQuery(queryBytes, queryBytes.length);

            DataInputStream responseStream = new DataInputStream(
                    new ByteArrayInputStream(singleResponse));

            responseStream.skipBytes(12);

            String domainName = readDomainName(singleResponse, responseStream, 12);
            responseStream.readShort();
            responseStream.readShort();

            String answerName = readDomainName(singleResponse, responseStream, 12);
            short answerType = responseStream.readShort();
            short answerClass = responseStream.readShort();
            int ttl = responseStream.readInt();
            short rdLength = responseStream.readShort();

            byte[] rdata = new byte[rdLength];
            responseStream.readFully(rdata);

            new Answer(domainNames.get(i), answerType, answerClass, ttl, rdLength, rdata)
                    .addAnswer(combinedResponse);

        }

        byte[] finalResponse = new byte[combinedResponse.position()];
        combinedResponse.flip();
        combinedResponse.get(finalResponse);

        return finalResponse;

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