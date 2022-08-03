package xyz.urffer.genetrees2.simulation;

import java.awt.Color;
import java.awt.Graphics;

import xyz.urffer.genetrees2.framework.GeneTrees;

public class GeneSeed {
	private int x;
	private int y;
	private GeneTree t;
	
	public GeneSeed(int x, int y, GeneTree tree) {
		this.x = x;
		this.y = y;
		this.t = tree;
	}

	public void draw(Graphics g) {
		int xScr = GeneTrees.panel.getXScr();
		int yScr = GeneTrees.panel.getYScr();
		
		g.setColor(Color.DARK_GRAY);
		g.drawRect(x - xScr - 1, y - yScr - 1, 3, 3);
	}
	
	public void tick() {
		y++;
	}
	
	public int getXPos() {
		return x;
	}
	
	public int getYPos() {
		return y;
	}
	
	public GeneTree getTree() {
		return t;
	}
}
