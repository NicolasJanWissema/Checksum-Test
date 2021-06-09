import java.io.*;
import java.net.*;
import java.util.Scanner;

public class ServerSide {
    private final DatagramSocket socket;

    public ServerSide(int port) throws SocketException {
        socket = new DatagramSocket(port);
    }
    public static void main(String[] args) {
        if (args.length != 1) {
            System.out.println("Syntax: ChatServer <port>");
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
    }

    /**
     *
     * @throws IOException What an exception...
     */
    private void service() throws IOException {
        //Begin checking for client packages.
        while (true) {
            byte[] data = new byte[512];
            DatagramPacket request = new DatagramPacket(data, data.length);

            //call method to send information.

        }
    }
}