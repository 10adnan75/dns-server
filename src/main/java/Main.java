import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;

public class Main {
  public static void main(String[] args) {

    System.out.println("Logs from your program will appear here!");

    try (DatagramSocket serverSocket = new DatagramSocket(2053)) {

      while (true) {

        final byte[] buf = new byte[512];
        final DatagramPacket packet = new DatagramPacket(buf, buf.length);

        serverSocket.receive(packet);

        System.out.println("Received data");

        Header defaultHeader = new Header((short)1234, (short)1, (short)0, (short)0, (short)0, (short)0, (short)0, (short)0, (short)0, (short)0, (short)0);

        byte[] requestData = packet.getData();
        int requestLength = packet.getLength();

        final byte[] bufResponse = defaultHeader.getResponse(requestData, requestLength);
        final DatagramPacket packetResponse = new DatagramPacket(bufResponse, bufResponse.length, packet.getSocketAddress());

        serverSocket.send(packetResponse);
      }
    } catch (IOException e) {

      System.out.println("IOException: " + e.getMessage());

    }

  }

}
