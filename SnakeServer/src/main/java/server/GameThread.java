package server;

import java.util.Observable;
import java.util.Observer;

import controller.ControllerSnakeGame;
import model.InputMap;
import model.SnakeGame;
import strategy.Strategy;
import strategy.StrategyHuman;

public class GameThread implements Runnable{
	private SnakeGame snakeGame;
	private String layoutName = "SnakeServer/layouts/alone/smallNoWall_alone.lay";
	private InputMap inputMap = null;
	Observer serverObserv;
	
	public enum direction{
		GAUCHE,
		DROITE,
		HAUT,
		BAS;
	}
	public GameThread(Observer observer) {
		serverObserv=observer;
		try {
			inputMap = new InputMap(layoutName);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		snakeGame = new SnakeGame(1000, inputMap, true);
		Strategy[] arrayStrategies = new Strategy[inputMap.getStart_snakes().size()];
		arrayStrategies[0]=new StrategyHuman();
		snakeGame.setStrategies(arrayStrategies);
		snakeGame.init();
	}
	
	public void run() {
		
	}
	public void update(Observable arg0, Object arg1) {
		// TODO Auto-generated method stub
		
	}
}
