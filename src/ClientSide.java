import java.io.IOException;
import java.net.*;
import java.nio.charset.StandardCharsets;
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
            port = Integer.parseInt(args[1]);
        }
        catch (NumberFormatException ex){
            System.out.println("Syntax: ChatServer <port> where port is integer");
            return;
        }
        String hostIP = args[0];

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
                long checkSum = getCRC32Checksum(message.getBytes());
                System.out.println("message Checksum: "+checkSum);
                System.out.println("Sending message to server...");
                try {
                    DatagramSocket socket = new DatagramSocket();
                    socket.setSoTimeout(5000);

                    InetAddress serverAddress = InetAddress.getByName(hostname);
                    sendRequest(message,socket);

                    System.out.println("Waiting for server input...");
                    String responseData = packetStep(socket);
                    System.out.println("Response from server: "+responseData); //test for response
                    System.out.println("Response Checksum: "+getCRC32Checksum(responseData.getBytes()));

                    if (getCRC32Checksum(responseData.getBytes())==checkSum){
                        System.out.println("Response CheckSum equal to original message Checksum. Checksum working.");
                    }
                    else{
                        System.out.println("Response not CheckSum equal to original message Checksum. Checksum not working.");
                    }

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

    public String packetStep(DatagramSocket socket) throws IOException {
        byte[] buffer = new byte[512];
        DatagramPacket response = new DatagramPacket(buffer, buffer.length);
        socket.receive(response);
        String responseData = new String(buffer, 0, response.getLength()).trim(); // extracts the response data in the form of a string"
        int numPackets = Integer.parseInt(responseData.split("‖")[0].split("/")[1]); // extracts the total number of packets from responseData

        if(numPackets != 1){
            StringBuilder fullResponse = new StringBuilder();

            String[] m = new String[numPackets]; // an array to ensure correct placement of messages
            int place = Integer.parseInt(responseData.split("‖")[0].split("/")[0])-1; // the packet number of the given response
            m[place] = responseData.split("‖")[1].trim(); // places the message in the correct position in the array
            try {
                for(int i = 0; i<(numPackets-1); i++){ // loop to ensure all responses are received and placed correctly according to packet number
                    socket.receive(response);
                    responseData = new String(buffer, 0, response.getLength()).trim();
                    place = Integer.parseInt(responseData.split("‖")[0].split("/")[0])-1;
                    m[place] = responseData.split("‖")[1].trim();
                }
                for(int j = 0; j<numPackets; j++){
                    fullResponse.append(m[j]);
                }
            } catch (SocketTimeoutException ex) {
                System.out.println("Timeout error: " + ex.getMessage());
                ex.printStackTrace();
            } catch (IOException ex) {
                System.out.println("Client error: " + ex.getMessage());
                ex.printStackTrace();
            }
            return fullResponse.toString();
        }
        else {
            return(responseData.split("‖")[1].trim());
        }
    }

    private void sendRequest(String ans, DatagramSocket socket) throws IOException{
        String[] packs;
        int messageSize = 480;
        if(ans.length()> messageSize){
            int packNum = ans.length()/ messageSize;
            packs = new String[packNum+1];
            int extra = ans.length()-ans.length()% messageSize;
            int temp=0;
            for(int i=0;i< extra;i+= messageSize){
                packs[temp] = ans.substring(i, i+ messageSize);
                temp++;
            }
            packs[packNum] = ans.substring(extra);
        }
        else{
            packs=new String[1];
            packs[0] = ans;
        }
        //Send packets
        InetAddress serverAddress = InetAddress.getByName(hostname);
        for (int i=0;i< packs.length;i++){
            byte[] buffer = (i+1+"/"+ packs.length+"‖"+packs[i]).getBytes();
            DatagramPacket response = new DatagramPacket(buffer, buffer.length, serverAddress, port);
            socket.send(response);
        }
    }

    private static long getCRC32Checksum(byte[] bytes) {
        Checksum crc32 = new CRC32();
        crc32.update(bytes, 0, bytes.length);
        return crc32.getValue();
    }
}
