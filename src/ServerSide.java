import java.io.*;
import java.net.*;
import java.util.Scanner;
import java.util.zip.CRC32;
import java.util.zip.Checksum;

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
        try{
            ServerSide server = new ServerSide(Integer.parseInt(args[0]));
            server.service();
        }
        catch (NumberFormatException ex){
            System.out.println("Syntax: ChatServer <port> where port is integer");
            return;
        }
        catch (SocketException ex) {
            System.out.println("Socket error: " + ex.getMessage());
        }
        catch (IOException ex) {
            System.out.println("I/O error: " + ex.getMessage());
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
            String clientData = packetStep(request,data);
            System.out.println("message from client: "+clientData);
            System.out.println("Checksum of message: "+getCRC32Checksum(clientData.getBytes()));


            //call method to send information.
            sendResponse(clientData,request);
        }
    }

    private void sendResponse(String ans, DatagramPacket request) throws IOException{
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
        InetAddress clientAddress = request.getAddress();
        int clientPort = request.getPort();
        for (int i=0;i< packs.length;i++){
            byte[] buffer = (i+1+"/"+ packs.length+"‖"+packs[i]).getBytes();
            DatagramPacket response = new DatagramPacket(buffer, buffer.length, clientAddress, clientPort);
            socket.send(response);
        }
    }

    private String packetStep(DatagramPacket request,byte[] data){
        try {
            socket.receive(request);
            String clientData = new String(data, 0, data.length).trim();
            int numPackets = Integer.parseInt(clientData.split("‖")[0].split("/")[1]); // extracts the total number of packets from responseData
            if(numPackets != 1){

                StringBuilder fullResponse = new StringBuilder();
                String[] statement = new String[numPackets]; // an array to ensure correct placement of messages
                int place = Integer.parseInt(clientData.split("‖")[0].split("/")[0])-1; // the packet number of the given response
                statement[place] = clientData.split("‖")[1].trim(); // places the message in the correct position in the array

                for(int i = 0; i<(numPackets-1); i++){ // loop to ensure all responses are received and placed correctly according to packet number
                    socket.receive(request);
                    clientData = new String(data, 0, request.getLength()).trim();
                    place = Integer.parseInt(clientData.split("‖")[0].split("/")[0])-1;
                    statement[place] = clientData.split("‖")[1].trim();
                }
                for(int j = 0; j<numPackets; j++){
                    fullResponse.append(statement[j]);
                }
                return(fullResponse.toString());
            }
            else{
                return (clientData.split("‖")[1]);
            }

        } catch (SocketTimeoutException ex) {
            System.out.println("Timeout error: " + ex.getMessage());
            ex.printStackTrace();
        } catch (IOException ex) {
            System.out.println("Client error: " + ex.getMessage());
            ex.printStackTrace();
        } catch(Exception exception){
            exception.printStackTrace();
        }
        System.out.println("FAIL?!");
        return("FAIL");
    }

    private static long getCRC32Checksum(byte[] bytes) {
        Checksum crc32 = new CRC32();
        crc32.update(bytes, 0, bytes.length);
        return crc32.getValue();
    }
}