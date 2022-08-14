package xyz.urffer.genetrees2.environment;

import java.awt.Color;
import java.awt.Graphics;

import xyz.urffer.genetrees2.framework.GeneTrees;
import xyz.urffer.genetrees2.framework.ParameterLoader;
import xyz.urffer.genetrees2.framework.ParameterNames;

public class SunSpeck extends Particle {
	
	public SunSpeck(int x, int y) {
		super(x, y, (long)ParameterLoader.getParam("particles", ParameterNames.SUNSPECK_BASE_POWER));
	}

	public void draw(Graphics g, int xScr, int yScr) {
		if (this.isConsumed) {
			g.setColor(Color.BLACK);
		} else {
			g.setColor(Color.YELLOW);
		}
		g.drawRect(x - xScr - 1, y - yScr - 1, 3, 3);
		
		if (GeneTrees.debug) {
			g.drawString("" + power, x - xScr, y - yScr);
		}
	}
	
	public void tick() {
		y++;
		power += (long)ParameterLoader.getParam("particles", ParameterNames.SUNSPECK_TICK_POWER_DELTA);
	}
}