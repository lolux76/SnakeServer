package server;

import netscape.javascript.JSObject;
import org.apache.commons.lang3.RandomStringUtils;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONStringer;

import controller.ControllerSnakeGame;
import model.InputMap;
import model.SnakeGame;
import server_state.ServerState;
import strategy.Strategy;
import strategy.StrategyHuman;
import view.PanelSnakeGame;
import view.ViewSnakeGame;

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
                String verif=outputString.split(": ")[1];
                if (verif.equals("/logout")) {
                    throw new SocketException();
                }
                else if(verif.equals("/run")) {
                    launchGame();
                }
                if (!clientNameList.containsKey(socket)) {
                    String[] messageString = outputString.split(":", 2);
                    clientNameList.put(socket, messageString[0]);
                    clientTokenList.put(socket, RandomStringUtils.randomAlphanumeric(250)); // Generate random token for identifying the client
                    System.out.println(messageString[0] + messageString[1]);
                    showMessageToAllClients(socket, messageString[0] + messageString[1]);
                } else {
                    showMessageToAllClients(socket, outputString);
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
    	System.out.println("launching the game");
        serverState = ServerState.IN_GAME;
        String layoutName = "SnakeServer/layouts/alone/smallNoWall_alone.lay";
		InputMap inputMap = null;
		try {
			inputMap = new InputMap(layoutName);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		SnakeGame snakeGame = new SnakeGame(1000, inputMap, true);
		Strategy[] arrayStrategies = new Strategy[inputMap.getStart_snakes().size()];
		arrayStrategies[0]=new StrategyHuman();
		snakeGame.setStrategies(arrayStrategies);
		snakeGame.init();
		ControllerSnakeGame controllerSnakeGame = new ControllerSnakeGame(snakeGame);
		PanelSnakeGame panelSnakeGame = new PanelSnakeGame(inputMap.getSizeX(), inputMap.getSizeY(), inputMap.get_walls(), inputMap.getStart_snakes(), inputMap.getStart_items());
		ViewSnakeGame view = new ViewSnakeGame(controllerSnakeGame, snakeGame, panelSnakeGame);
		snakeGame.launch();
		controllerSnakeGame.play();
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
                    JSONObject json= new JSONObject();
                    json.put("token",clientTokenList.get(socket));
                    json.put("message",outputString);
                    printWriter.println(json);
                }
            } catch (IOException ex) {
                System.out.println(ex);
            } catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
        }
    }
}
