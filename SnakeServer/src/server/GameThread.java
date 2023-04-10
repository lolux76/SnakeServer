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
import utils.Position;

public class GameThread extends Thread implements Observer {
	private SnakeGame snakeGame;
	private String layoutName = "layouts/alone/smallNoWall_alone.lay";
	private InputMap inputMap = null;
	Observer serverObserv;

	private String token;
	private Socket socket;
	private GameObserveur gameObserveur;
	
	public GameThread(Socket socket, String token) {
		this.socket = socket;
		this.token = token;

		try {
			inputMap = new InputMap(layoutName);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		this.gameObserveur = new GameObserveur();
		this.gameObserveur.addObserver(this);
		this.snakeGame = new SnakeGame(1000, inputMap, true, this.gameObserveur);
		Strategy[] arrayStrategies = new Strategy[inputMap.getStart_snakes().size()];
		arrayStrategies[0]=new StrategyHuman();
		this.snakeGame.setStrategies(arrayStrategies);
		this.snakeGame.init();
	}
	
	public void run() {
		while(true) {
			snakeGame.takeTurn();
			try {
				Thread.sleep(snakeGame.getTime());
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
	
	public void majSnake(int direction){
		snakeGame.changeLastAction(Direction.values()[direction]);
	}

	@Override
	public void update(Observable observable, Object o) {
		this.snakeGame = (SnakeGame)o;
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
			JSONArray positionsX= new JSONArray();
			JSONArray positionsY= new JSONArray();
			for(Position position: snake.getPositions()) {
				positionsX.put(position.getX());
				positionsY.put(position.getY());
			}
			JSONObject positionJSON= new JSONObject();
			positionJSON.put("x", positionsX);
			positionJSON.put("y", positionsY);
			snakesPositions.put(positionJSON);
		}
		jsonData.put("snakes" , snakesPositions);

		JSONArray itemsPositions = new JSONArray();
		for(Item item : items){
			JSONObject positionJSON = new JSONObject();
			positionJSON.put("itemType", item.getItemType().ordinal()); //numéro dans l'énumération
			positionJSON.put("x", item.getX());
			positionJSON.put("y", item.getY());
			itemsPositions.put(positionJSON);
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
