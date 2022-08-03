package xyz.urffer.genetrees2.simulation;

import java.awt.Color;
import java.awt.Graphics;
import java.util.ArrayList;
import java.util.HashSet;

import xyz.urffer.genetrees2.framework.GeneTrees;

public class GeneTree implements Comparable<GeneTree> {
	private long fitness = 0;
	private long nutrients = 0;
	private long energy = 0;
	private double fitnessPercentage = 0;

	private TreeNode root; // this tree's base node
	private HashSet<TreeNode> nodes; // the set of this tree's nodes
	private int age; // the number of mutations this tree is from generation 0

	private int xMin = 0, xMax = 0, yMin = 0, yMax = 0, width, height;

	// creates a new genetree with one root and one other node
	public GeneTree(int x, int y) {
		root = new TreeNode(x, y, this, null);
		this.init();

		nodes = root.getNodes();

		age = 0;
	}

	// creates a new, blank genetree with a newly-assembled node structure
	// rooted at the given node
	// for I/O purposes
	public GeneTree(TreeNode n) {
		root = n;
		this.init();

		nodes = root.getNodes();
	}

	// create a new genetree as a child of the given one
	public GeneTree(GeneTree t, int x, int y) {
		root = new TreeNode(x, y, this, null, t.getRoot());
		root.mutate();
		this.init();

		nodes = root.getNodes();

		age = t.getAge() + 1;
	}

	private void init() {
		root.setOwner(this);
		root.initLocation();
		width = Math.abs(xMax) - Math.abs(xMin);
		height = Math.abs(yMax) - Math.abs(yMin);
	}
	
	public void draw(Graphics g) {
		draw(g, GeneTrees.panel.getXScr(), GeneTrees.panel.getYScr());
	}

	public void draw(Graphics g, int xScr, int yScr) {
		for (TreeNode n : nodes) {
			// if this isn't the root node, draw its branch to its parent
			if (n.getParent() != null) {
				g.setColor(Color.BLACK);
				g.drawLine(n.getXPos() - xScr, n.getYPos() - yScr, n.getParent().getXPos() - xScr,
						n.getParent().getYPos() - yScr);
			}

			switch (n.getType()) {
			case Struct:
				g.setColor(Color.BLACK);
				break;
			case Leaf:
				g.setColor(new Color(52, 237, 52));
				break;
			case Root:
				g.setColor(new Color(137, 47, 4));
				break;
			case Raincatcher:
				g.setColor(Color.BLUE.brighter().brighter());
				break;
			case SeedDropper:
				g.setColor(Color.YELLOW.darker().darker());
				break;
			default:
				System.out.println("node: " + n.getType());
				throw new IllegalArgumentException("invalid node type");
			}

			int xTL = n.getXPos() - n.getSize() / 2 - xScr;
			int yTL = n.getYPos() - n.getSize() / 2 - yScr;
			g.fillOval(xTL, yTL, n.getSize(), n.getSize());

			// if debug mode, draw bounding boxes
			if (GeneTrees.debug) {
				// standard bounding box
				g.setColor(Color.WHITE);
				g.drawRect(xTL, yTL, n.getSize(), n.getSize());

				// if the mouse is over this node
				if (GeneTrees.panel.getMouseX() > xTL && GeneTrees.panel.getMouseX() < xTL + n.getSize()
						&& GeneTrees.panel.getMouseY() > yTL && GeneTrees.panel.getMouseY() < yTL + n.getSize()) {
					g.setColor(Color.BLACK); // highlight it
					g.drawRect(xTL, yTL, n.getSize(), n.getSize());

					// highlight its parent
					if (n.getParent() != null) {
						int s = n.getParent().getSize() + 2;
						xTL = n.getParent().getXPos() - s / 2;
						yTL = n.getParent().getYPos() - s / 2;
						g.setColor(Color.CYAN);
						g.drawRect(xTL, yTL, s, s);
					}

					// hightlight its children
					for (TreeNode nc : n.getChildren()) {
						int s = nc.getSize() + 2;
						xTL = nc.getXPos() - s / 2;
						yTL = nc.getYPos() - s / 2;
						g.setColor(Color.MAGENTA);
						g.drawRect(xTL, yTL, s, s);
					}
				}
			}

			if (GeneTrees.debug) {
				g.setColor(Color.BLUE);
				g.drawRect(xMin - xScr, yMin - yScr, width, height);
			}

			if (GeneTrees.panel.getSimulation().getTrackedTree() == this) {
				g.setColor(Color.ORANGE);
				g.drawRect(xMin - xScr, yMin - yScr, width, height);
			}
		}
	}

	// gather nutrients through root nodes, and calculate fitness decay
	public void tick() {
		for (TreeNode n : nodes) {
			// if this is a root node, gradually increment its fitness
			if (n.getType() == NodeType.Root && n.getYPos() > GeneTrees.panel.getSimulation().getEnv().getGroundLevel(n.getXPos())) {
				nutrients += 3 * n.getSize();
			}

			// decrement fitness proportional to the size of this node,
			// moreso if it is a structure node
			if (n.getType() == NodeType.Struct) {
				fitness -= n.getSize() * 2;
			} else if (n.isActivated() || n.getType() == NodeType.Root) {
				fitness -= n.getSize();
			} else {
				fitness -= n.getSize() * 4;
			}
		}

		// calculate how much fitness has been gained this tick
		long newFitness = Math.min(energy, nutrients);
		if (newFitness > 0) {
			energy -= newFitness;
			nutrients -= newFitness;
			fitness += 2 * newFitness;
		}
	}

	/*
	 * getters and setters
	 */
	public long getFitness() {
		return fitness;
	}

	public TreeNode getRoot() {
		return root;
	}

	/*
	 * debugging
	 */
	@SuppressWarnings("unused")
	private void printTree() {
		for (TreeNode n : nodes) {
			System.out.println("x: " + n.getXPos());
			System.out.println("y: " + n.getYPos());
			System.out.println(" ");
		}
	}

	public int compareTo(GeneTree o) {
		return (int) (this.fitness - o.fitness);
	}

	public void resetFitness() {
		energy = 0;
		nutrients = 0;
		fitness = 0;
	}

	public void mutate() {
		root.mutate();
	}

	public int getNumNodes() {
		return nodes.size();
	}

	public ArrayList<TreeNode> getAllNodes() {
		return new ArrayList<TreeNode>(nodes);
	}

	public int getAge() {
		return age;
	}

	public void setAge(int a) {
		this.age = a;
	}

	synchronized public long getNutrients() {
		return nutrients;
	}
	
	synchronized public void setNutrients(long nu) {
		nutrients = nu;
	}

	synchronized public long getEnergy() {
		return energy;
	}
	
	synchronized public void setEnergy(long en) {
		energy = en;
	}

	public int getxMin() {
		return xMin;
	}

	public void setxMin(int xMin) {
		this.xMin = xMin;
	}

	public int getxMax() {
		return xMax;
	}

	public void setxMax(int xMax) {
		this.xMax = xMax;
	}

	public int getyMin() {
		return yMin;
	}

	public void setyMin(int yMin) {
		this.yMin = yMin;
	}

	public int getyMax() {
		return yMax;
	}

	public void setyMax(int yMax) {
		this.yMax = yMax;
	}

	public boolean isOverTree(int x, int y) {
		return (x > xMin && x < xMax && y > yMin && y < yMax);
	}

	synchronized public void setFitnessPercentage(double d) {
		fitnessPercentage = d;
	}

	synchronized public double getFitnessPercentage() {
		return fitnessPercentage;
	}
}