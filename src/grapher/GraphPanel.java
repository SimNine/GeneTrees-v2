package grapher;

import java.awt.Color;
import java.awt.Graphics;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;

import javax.swing.JPanel;

@SuppressWarnings("serial")
public class GraphPanel extends JPanel {
	HashMap<Integer, GraphDataset> dataSets = new HashMap<Integer, GraphDataset>();
	double xMin = -1;
	double xMax = 5;
	double yMin = -1;
	double yMax = 5;

	double xStep = 1.0;
	double yStep = 1.0;
	
	// repaints the panel with all the data
	public void paintComponent(Graphics g) {
		// clear the panel
		g.setColor(Color.WHITE);
		g.fillRect(0, 0, getWidth(), getHeight());
		System.out.println(xMin + "," + yMin + " " + xMax + "," + yMax);
		
		// draw axes
		int[] axisPos = pointToScreen(0, 0);
		
		if (axisPos[0] < 0) {
			axisPos[0] = 0;
		} else if (axisPos[0] > this.getWidth()) {
			axisPos[0] = this.getWidth() - 1;
		}
		
		if (axisPos[1] < 0) {
			axisPos[1] = 0;
		} else if (axisPos[1] > this.getHeight()) {
			axisPos[1] = this.getHeight() - 1;
		}
		
		g.setColor(Color.BLACK);
		g.drawLine(0, axisPos[1], getWidth(), axisPos[1]); // x-axis
		g.drawLine(axisPos[0], 0, axisPos[0], getHeight()); // y-axis
		
		// draw points
		for (GraphDataset d : dataSets.values()) {
			g.setColor(d.getColor());
			
			LinkedList<Pair<Double, Double>> ll = d.getPoints();
			for (int i = 0; i < ll.size() - 1; i++) {
				Pair<Double, Double> curr = ll.get(i);
				Pair<Double, Double> next = ll.get(i+1);
				
				int[] currPos = pointToScreen(curr.a, curr.b);
				int[] nextPos = pointToScreen(next.a, next.b);
				
				g.drawRect(currPos[0] - 1, currPos[1] - 1, 3, 3);
				g.drawLine(currPos[0], currPos[1], nextPos[0], nextPos[1]);
			}
		}
		
		// draw graph key
		g.setColor(Color.WHITE);
		g.fillRect(20, 20, 200, dataSets.values().size()*20 + 10);
		g.setColor(Color.BLACK);
		g.drawRect(20, 20, 200, dataSets.values().size()*20 + 10);
		int count = 0;
		for (GraphDataset d : dataSets.values()) {
			g.setColor(d.getColor());
			g.fillRect(30, 30 + count*20, 10, 10);
			g.setColor(Color.BLACK);
			g.drawString(d.getName(), 45, 40 + count*20);
			count++;
		}
	}
	
	private boolean isOnscreen(double x, double y) {
		if (x < xMin || x > xMax || y < yMin || y > yMax)
			return false;
		else
			return true;
	}
	
	// converts a data coord to a graphics coord
	private int[] pointToScreen(double x, double y) {
		double w = xMax - xMin;
		double h = yMax - yMin;
		
		double xDisp = x - xMin;
		double yDisp = y - yMin;
		
		int xPos = (int)((xDisp/w)*(this.getWidth()));
		int yPos = this.getHeight() - (int)((yDisp/h)*(this.getHeight()));
		
		int[] ret = new int[2];
		ret[0] = xPos;
		ret[1] = yPos;
		return ret;
	}
	
	// adds the given double point to the dataset with the given name
	public void addPoint(int setNum, double x, double y) {
		if (dataSets.containsKey(setNum)) {
			dataSets.get(setNum).addPoint(x, y);
		} else {
			System.out.println("err: tried adding to a nonexistent GraphDataset");
		}
	}
	
	// adds a dataset
	public void addDataset(int setID, String name, Color col) {
		if (dataSets.containsKey(setID)) {
			System.out.println("err: tried adding a dataset that is already present");
		} else {
			dataSets.put(setID, new GraphDataset(name, col));
		}
	}
	
	// removes the dataset with the given name
	public void removeSet(int setNum) {
		dataSets.remove(setNum);
	}
	
	/*
	 * various methods to deal with window scaling and resizing
	 */
	public void setXBounds(double min, double max) {
		xMin = min;
		xMax = max;
	}
	
	public void setXStep(double step) {
		xStep = step;
	}
	
	public void setYBounds(double min, double max) {
		yMin = min;
		yMax = max;
	}
	
	public void setYStep(double step) {
		yStep = step;
	}
}
