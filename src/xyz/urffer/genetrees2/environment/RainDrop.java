package xyz.urffer.genetrees2.environment;

import java.awt.Color;
import java.awt.Graphics;

import xyz.urffer.genetrees2.framework.GeneTrees;

public class RainDrop extends Particle {
	
	public RainDrop(int x, int y) {
		super(x, y, EnvironmentParameters.RAINDROP_BASE_POWER);
	}

	public void draw(Graphics g, int xScr, int yScr) {
		if (this.isConsumed) {
			g.setColor(Color.BLACK);
		} else {
			g.setColor(Color.BLUE);
		}
		g.drawRect(x - xScr - 1, y - yScr - 1, 3, 3);
		
		if (GeneTrees.debug) {
			g.drawString("" + power, x - xScr, y - yScr);
		}
	}
	
	public void tick() {
		y++;
		power += EnvironmentParameters.RAINDROP_TICK_POWER_DELTA;
	}
}
