package model;

import java.util.ArrayList;

import agent.Snake;
import item.Item;

public class Infos {
	ArrayList<Snake> snakes;
	ArrayList<Item> items;
	Infos(){
		snakes=null;
		items=null;
	}
	public void addSnake(Snake snake) {
		snakes.add(snake);
	}
	public void addItem(Item item) {
		items.add(item);
	}
	public ArrayList<Snake> getSnakes(){
		return snakes;
	}
	public ArrayList<Item> getItems(){
		return items;
	}
}
