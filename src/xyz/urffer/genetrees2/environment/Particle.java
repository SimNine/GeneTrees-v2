package xyz.urffer.genetrees2.environment;

import xyz.urffer.genetrees2.environment.genetree.TreeNode;

public abstract class Particle {
	protected int x;
	protected int y;
	protected long power;
	
	public Particle(int x, int y, long power) {
		this.x = x;
		this.y = y;
		this.power = power;
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
	
	public abstract void tick();
	
	public boolean collidesWithNode(TreeNode n) {
		int nx = n.getXPos();
		int ny = n.getYPos();
		int nr = n.getSize() / 2;

		return ((nx - x) * (nx - x) + (ny - y) * (ny - y) < nr * nr);
	}
	
}
