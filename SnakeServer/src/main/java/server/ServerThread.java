package server;

import netscape.javascript.JSObject;
import org.apache.commons.lang3.RandomStringUtils;
import org.json.JSONStringer;
import server_state.ServerState;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

public class ServerThread extends Thread{
    private Socket socket;
    private ArrayList<Socket> clients;
    private HashMap<Socket, String> clientNameList;
    private HashMap<Socket, String> clientTokenList;
    private ServerState serverState;
    private String messageColor;

    public ServerThread(Socket socket, ArrayList<Socket> clients, HashMap<Socket, String> clientNameList, HashMap<Socket, String> clientTokenList) {
        this.socket = socket;
        this.clients = clients;
        this.clientNameList = clientNameList;
        this.clientTokenList = clientTokenList;
        this.serverState = ServerState.LOBBY;
        this.messageColor = "";
    }

    @Override
    public void run() {
        try {
            BufferedReader input = new BufferedReader(new InputStreamReader(socket.getInputStream()));

            while (true) {
                String outputString = input.readLine();
                if (outputString.equals("/logout")) {
                    throw new SocketException();
                }
                else if(outputString.equals("/run")) {
                    launchGame();
                }
                if (!clientNameList.containsKey(socket)) {
                    String[] messageString = outputString.split(":", 2);
                    clientNameList.put(socket, messageString[0]);
                    clientTokenList.put(socket, RandomStringUtils.randomAlphanumeric(250)); // Generate random token for identifying the client
                    System.out.println(messageString[0] + messageString[1]);
                    showMessageToAllClients(socket, messageString[0] + messageString[1]);
                } else {
                    messageColor = ChatColor.getColor(clients.indexOf(socket));
                    System.out.println(messageColor + outputString + ChatColor.RESET);
                    showMessageToAllClients(socket, messageColor + outputString + ChatColor.RESET);
                }
            }
        } catch (SocketException e) {
            String printMessage = clientNameList.get(socket) + " left the chat room";
            System.out.println(printMessage);
            showMessageToAllClients(socket, printMessage);
            clients.remove(socket);
            clientNameList.remove(socket);
        } catch (Exception e) {
            System.out.println(Arrays.toString(e.getStackTrace()));
        }
    }

    private void launchGame() {
        serverState = ServerState.IN_GAME;
    }

    private void showMessageToAllClients(Socket sender, String outputString) {
        Socket socket;
        PrintWriter printWriter;
        int i = 0;
        while (i < clients.size()) {
            socket = clients.get(i);
            i++;
            try {
                if (socket != sender) {
                    printWriter = new PrintWriter(socket.getOutputStream(), true);
                    JSONStringer json = new JSONStringer();
                    json.object();
                    json.key("token").value(clientTokenList.get(socket));
                    json.key("message").value(outputString);
                    json.endObject();
                    printWriter.println(json.toString());
                }
            } catch (IOException ex) {
                System.out.println(ex);
            }
        }
    }
}
