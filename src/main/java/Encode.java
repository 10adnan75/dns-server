import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;

public class Encode {
    

    public byte[] encodeDomainName(String domainName) {

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

        for (String label: domainName.split("\\.")) {

            outputStream.write((byte)label.length());
            outputStream.writeBytes(label.getBytes(StandardCharsets.UTF_8));

        }

        outputStream.write(0x00);

        return outputStream.toByteArray();

    }

}
