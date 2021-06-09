import java.io.IOException;
import java.net.*;
import java.util.Scanner;
import java.util.zip.CRC32;
import java.util.zip.Checksum;

public class ClientSide {
    private String hostname;
    private int port;

    private ClientSide(String host, int port) {
        this.hostname = host;
        this.port = port;
    }

    public static void main(String[] args) {
        if (args.length != 2) {
            System.out.println("Syntax: ChatServer <host IP> <port>");
            return;
        }

        int port;
        try{
            port = Integer.parseInt(args[0]);
        }
        catch (NumberFormatException ex){
            System.out.println("Syntax: ChatServer <port> where port is integer");
            return;
        }
        String hostIP = args[1];

        ClientSide server = new ClientSide(hostIP,port);
        server.service();
    }
    private void service(){
        Scanner scanner = new Scanner(System.in);
        boolean check=true;

        while(check){
            System.out.println("Enter message to send to server or EXIT to stop program.");
            String message = scanner.nextLine();
            if (message.compareTo("EXIT")==0){
                System.out.println("Exiting...");
                check=false;
            }
            else{
                System.out.println("message: "+message);
                System.out.println("Sending message to server...");
                try {
                    DatagramSocket socket = new DatagramSocket();
                    socket.setSoTimeout(5000);

                    InetAddress serverAddress = InetAddress.getByName(hostname);
                    byte[] buffer = (message).getBytes();
                    System.out.println(getCRC32Checksum(buffer));
                    DatagramPacket request = new DatagramPacket(buffer, buffer.length, serverAddress, port);
                    socket.send(request);

                    DatagramPacket response = new DatagramPacket(buffer, buffer.length);
                    socket.receive(response);
                    byte[] data = new byte[512];
                    String clientData = new String(data, 0, data.length);
                    System.out.println(data.toString());
                    socket.close();
                }
                catch (SocketTimeoutException ex) {
                    System.out.println("Timeout with server.");
                }
                catch (IOException ex) {
                    System.out.println("Client error: " + ex.getMessage());
                    ex.printStackTrace();
                }
            }
        }
    }

    private static long getCRC32Checksum(byte[] bytes) {
        Checksum crc32 = new CRC32();
        crc32.update(bytes, 0, bytes.length);
        return crc32.getValue();
    }
}
