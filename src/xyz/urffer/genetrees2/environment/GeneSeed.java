package xyz.urffer.genetrees2.environment;

import java.awt.Color;
import java.awt.Graphics;

import xyz.urffer.genetrees2.environment.genetree.GeneTree;

public class GeneSeed {
	private int x;
	private int y;
	private GeneTree t;
	
	public GeneSeed(int x, int y, GeneTree tree) {
		this.x = x;
		this.y = y;
		this.t = tree;
	}

	public void draw(Graphics g, int xScr, int yScr) {
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
