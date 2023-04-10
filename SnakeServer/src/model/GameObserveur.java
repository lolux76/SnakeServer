package model;

import java.util.Observable;

public class GameObserveur extends Observable {

    public void update(SnakeGame snakeGame){
    	setChanged();
        notifyObservers(snakeGame);
    }
}
