package server;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Observable;
import java.util.Observer;

import agent.Snake;
import controller.ControllerSnakeGame;
import item.Item;
import model.GameObserveur;
import model.InputMap;
import model.SnakeGame;
import org.json.JSONArray;
import org.json.JSONObject;
import strategy.Strategy;
import strategy.StrategyHuman;
import utils.Direction;

public class GameThread implements Runnable, Observer {
	private SnakeGame snakeGame;
	private String layoutName = "SnakeServer/layouts/alone/smallNoWall_alone.lay";
	private InputMap inputMap = null;
	Observer serverObserv;

	private String token;
	private Socket socket;
	private GameObserveur gameObserveur;
	
	public GameThread(Observer observer, Socket socket, String token) {
		serverObserv=observer;

		this.socket = socket;
		this.token = token;

		try {
			inputMap = new InputMap(layoutName);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		this.gameObserveur = new GameObserveur();
		this.snakeGame = new SnakeGame(1000, inputMap, true, this.gameObserveur);
		Strategy[] arrayStrategies = new Strategy[inputMap.getStart_snakes().size()];
		arrayStrategies[0]=new StrategyHuman();
		this.snakeGame.setStrategies(arrayStrategies);
		this.snakeGame.init();
		this.gameObserveur.addObserver(this);
	}
	
	public void run() {
		snakeGame.takeTurn();
	}
	
	public void majSnake(Direction direction){
		snakeGame.changeLastAction(direction);
	}

	@Override
	public void update(Observable observable, Object o) {
		ArrayList<Snake> snakes = this.snakeGame.getSnakes();
		boolean[][] walls = this.snakeGame.getWalls();
		ArrayList<Item> items = this.snakeGame.getItems();
		try {
			sendGameState(snakes, walls, items);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	private void sendGameState(ArrayList<Snake> snakes, boolean[][] walls, ArrayList<Item> items) throws IOException {
		PrintWriter printWriter = new PrintWriter(this.socket.getOutputStream(), true);
		JSONObject jsonData = new JSONObject();
		jsonData.put("token", this.token);
		JSONArray snakesPositions = new JSONArray();
		for(Snake snake : snakes){
			JSONArray position = new JSONArray();
			JSONObject positionJSON = new JSONObject();
			positionJSON.put("x", snake.getX());
			positionJSON.put("y", snake.getY());
			position.put(positionJSON);
			snakesPositions.put(position);
		}
		jsonData.put("snakes" , snakesPositions);

		JSONArray itemsPositions = new JSONArray();
		for(Item item : items){
			JSONArray position = new JSONArray();
			JSONObject positionJSON = new JSONObject();
			positionJSON.put("itemType", item.getItemType().ordinal()); //numéro dans l'énumération
			positionJSON.put("x", item.getX());
			positionJSON.put("y", item.getY());
			position.put(positionJSON);
			itemsPositions.put(position);
		}
		jsonData.put("items", itemsPositions);

		JSONArray wallsArray = new JSONArray();
		for (boolean[] row : walls) {
			JSONArray rowArray = new JSONArray();
			for (boolean value : row) {
				rowArray.put(value);
			}
			wallsArray.put(rowArray);
		}
		jsonData.put("walls", wallsArray);

		printWriter.println(jsonData);

	}
}
