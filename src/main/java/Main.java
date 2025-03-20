import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;

public class Main {

  public static void main(String[] args) {

    String resolverAddress = null;

    for (int i = 0; i < args.length - 1; i++) {

      if (args[i].equals("--resolver")) {

        resolverAddress = args[i + 1];
        break;

      }

    }

    if (resolverAddress == null) {

      System.out.println("Resolver address not provided. Use --resolver <address>");
      return;

    }

    System.out.println("Forwarding DNS server started with resolver: " + resolverAddress);

    String[] addressParts = resolverAddress.split(":");
    String resolverIp = addressParts[0];
    int resolverPort = Integer.parseInt(addressParts[1]);

    try (DatagramSocket serverSocket = new DatagramSocket(2053)) {

      while (true) {

        final byte[] buf = new byte[512];
        final DatagramPacket packet = new DatagramPacket(buf, buf.length);

        serverSocket.receive(packet);

        System.out.println("\n -------- CLIENT -------- ");
        System.out.println(" Data length : " + packet.getLength());
        System.out.println(" [ " + new String(buf, 0, packet.getLength()) + " ]");
        System.out.println(" Resolver IP : " + resolverIp);

        final byte[] bufResponse = new Parser(resolverIp, resolverPort).parse(packet);
        final DatagramPacket packetResponse = new DatagramPacket(bufResponse, bufResponse.length,
            packet.getSocketAddress());

        System.out.println("\n -------- SERVER -------- ");
        System.out.println(" Data length : " + packetResponse.getLength());
        System.out.println(" [ " + new String(bufResponse, 0, packetResponse.getLength()) + " ]");
        System.out.println("\n --------  *  *  -------- \n");

        serverSocket.send(packetResponse);

      }
    } catch (IOException e) {

      System.out.println("IOException: " + e.getMessage());

    }

  }

}