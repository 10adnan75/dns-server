import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.nio.ByteBuffer;

public class Main {
  public static void main(String[] args) {

    System.out.println("Logs from your program will appear here!");

    try (DatagramSocket serverSocket = new DatagramSocket(2053)) {

      while (true) {

        final byte[] buf = new byte[512];
        final DatagramPacket packet = new DatagramPacket(buf, buf.length);

        serverSocket.receive(packet);

        System.out.println("Received data");

        ByteBuffer DNSMessage = ByteBuffer.allocate(512);

        Header header = new Header((short)1234, (short)1, (short)0, (short)0, (short)0, (short)0, (short)0, (short)1, (short)0, (short)0, (short)0);
        header.addHeader(DNSMessage);

        Question question = new Question("codecrafters.io", (short)1, (short)1);
        question.addQuestion(DNSMessage);

        final byte[] bufResponse = DNSMessage.array();
        final DatagramPacket packetResponse = new DatagramPacket(bufResponse, bufResponse.length, packet.getSocketAddress());

        serverSocket.send(packetResponse);

      }
    } catch (IOException e) {

      System.out.println("IOException: " + e.getMessage());

    }

  }

}
