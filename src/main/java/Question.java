import java.io.ByteArrayOutputStream;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;

public class Question implements DNSQuestion {

    private String name;
    private short qType;
    private short qClass;

    public Question(String name, short qType, short qClass) {

        this.name = name;
        this.qType = qType;
        this.qClass = qClass;

    }

    public void addQuestion(ByteBuffer DNSMessage) {

        DNSMessage.put(encodeDomainName(this.name));
        DNSMessage.putShort(this.qType);
        DNSMessage.putShort(this.qClass);

        return;

    }

    public byte[] encodeDomainName(String domainName) {

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

        for (String label: domainName.split("\\.")) {

            outputStream.write((byte)label.length());
            outputStream.writeBytes(label.getBytes(StandardCharsets.UTF_8));

        }

        outputStream.write(0);

        return outputStream.toByteArray();

    }
}
