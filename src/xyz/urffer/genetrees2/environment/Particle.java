package xyz.urffer.genetrees2.environment;

import xyz.urffer.genetrees2.environment.genetree.GeneTree;
import xyz.urffer.genetrees2.environment.genetree.TreeNode;

public abstract class Particle {
	protected int x;
	protected int y;
	protected long power;
	protected boolean isConsumed;
	
	public Particle(int x, int y, long power) {
		this.x = x;
		this.y = y;
		this.power = power;
		this.isConsumed = false;
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
	
	public boolean collidesWithTree(GeneTree t) {
		if (this.x < t.getxMin() || this.x > t.getxMax() ||
			this.y < t.getyMin() || this.y > t.getyMax()) {
			return false;
		} else {
			return true;
		}
	}
	
	public boolean collidesWithNode(TreeNode n) {
		int nr = n.getSize() / 2;
		int nx = n.getPos()[0] + nr;
		int ny = n.getPos()[1] + nr;

		return ((nx - x) * (nx - x) + (ny - y) * (ny - y) < nr * nr);
	}
	
	public boolean isConsumed() {
		return this.isConsumed;
	}
	
	public void consume() {
		this.isConsumed = true;
	}
	
}
