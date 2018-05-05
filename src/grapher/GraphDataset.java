package grapher;

import java.awt.Color;
import java.util.LinkedList;

public class GraphDataset {
	LinkedList<Pair<Double, Double>> points;
	String name;
	Color color;
	
	
	public GraphDataset(String name, Color color) {
		this.name = name;
		this.color = color;
		this.points = new LinkedList<Pair<Double, Double>>();
	}
	
	public Color getColor() {
		return color;
	}
	
	public String getName() {
		return name;
	}
	
	public void addPoint(double x, double y) {
		points.addLast(new Pair<Double, Double>(x, y));
	}
	
	public LinkedList<Pair<Double, Double>> getPoints() {
		return points;
	}
}
