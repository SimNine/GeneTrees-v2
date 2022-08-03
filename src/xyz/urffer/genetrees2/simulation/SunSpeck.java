package xyz.urffer.genetrees2.simulation;
import java.awt.Color;
import java.awt.Graphics;

import xyz.urffer.genetrees2.framework.GeneTrees;

public class SunSpeck {
	private int x;
	private int y;
	private long power;
	
	public SunSpeck(int x, int y) {
		this.x = x;
		this.y = y;
		this.power = 50000;
	}

	public void draw(Graphics g) {
		int xScr = GeneTrees.panel.getXScr();
		int yScr = GeneTrees.panel.getYScr();
		
		g.setColor(Color.YELLOW);
		g.drawRect(x - xScr - 1, y - yScr - 1, 3, 3);
		
		if (GeneTrees.debug) {
			g.drawString("" + power, x - xScr, y - yScr);
		}
	}
	
	public void tick() {
		y++;
		power -= 80;
	}
	
	public int getXPos() {
		return x;
	}
	
	public int getYPos() {
		return y;
	}
	
	public long getPower() {
		return power;
	}
}