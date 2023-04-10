package server;

import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

public class Server {
    public static void main(String[] args) {
        ArrayList<Socket> clients = new ArrayList<>();
        HashMap<Socket, String> clientNameList = new HashMap<Socket, String>();
        HashMap<Socket, String> clientTokenList = new HashMap<Socket, String>();
        try (ServerSocket serversocket = new ServerSocket(8082)) {
            System.out.println("Server listening on port: 8082");
            while (true) {
                Socket socket = serversocket.accept();
                clients.add(socket);
                ServerThread serverThread = new ServerThread(socket, clients, clientNameList, clientTokenList);
                serverThread.start();
            }
        } catch (Exception e) {
            System.out.println(Arrays.toString(e.getMessage().toCharArray()));
        }
    }
}
