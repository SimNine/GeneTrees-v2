package xyz.urffer.genetrees2.environment.genetree;

import java.awt.Color;
import java.awt.Graphics;
import java.util.ArrayList;
import java.util.Random;

import xyz.urffer.genetrees2.environment.EnvironmentParameters;
import xyz.urffer.genetrees2.framework.GeneTrees;

public class GeneTree implements Comparable<GeneTree> {
	private long fitness = 0;
	private long nutrients = 0;
	private long energy = 0;
	private double fitnessPercentage = 0;

	private TreeNode root;
	private int age; // the number of mutations this tree is from generation 0

	private int xMin = 0, xMax = 0, yMin = 0, yMax = 0;
	
	private Random random;
	

	/**
	 * Loads a genetree with a newly-assembled node structure rooted at the given node. Used for loading trees saved to file.
	 * 
	 * @param random	The random number generator
	 * @param n			The node to use as a root
	 */
	public GeneTree(Random random, TreeNode n) {
		this.random = random;
		
		root = n;
		root.setOwner(this);
		root.initLocation();
	}

	/**
	 * Creates a completely new genetree
	 * 
	 * @param random	The random number generator
	 * @param x			x-position of this new genetree
	 * @param y			y-position of this new genetree
	 */
	public GeneTree(Random random, int x, int y) {
		this(random, new TreeNode(random, x, y, null, null));
		root.initLocation();

		age = 0;
	}

	/**
	 * Create a new genetree as a child of the given one. The child genetree will be a mutated version of its parent.
	 * 
	 * @param random		The random number generator
	 * @param parentTree	The parent tree to create a mutated child of
	 * @param x				x-position of this new genetree
	 * @param y				y-position of this new genetree
	 */
	public GeneTree(Random random, GeneTree parentTree, int x, int y) {
		this(random, new TreeNode(random, x, y, null, null, parentTree.getRoot()));
		root.mutate();
		root.initLocation();

		age = parentTree.getAge() + 1;
	}
	

	public void draw(Graphics g, int xScr, int yScr) {
		for (TreeNode n : root.getNodes()) {
			int nodeRadius = n.getSize() / 2;
			
			// if this isn't the root node, draw its branch to its parent
			if (n.getParent() != null) {
				int parentRadius = n.getParent().getSize() / 2;
				g.setColor(Color.BLACK);
				g.drawLine(n.getPos()[0] + nodeRadius - xScr, n.getPos()[1] + nodeRadius - yScr, 
						   n.getParent().getPos()[0] + parentRadius - xScr, n.getParent().getPos()[1] + parentRadius - yScr);
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

			int xTL = n.getPos()[0] - xScr;
			int yTL = n.getPos()[1] - yScr;
			g.fillOval(xTL, yTL, n.getSize(), n.getSize());

			// if debug mode, draw bounding boxes
			if (GeneTrees.debug) {
				// standard bounding box
				g.setColor(Color.WHITE);
				g.drawRect(xTL, yTL, n.getSize(), n.getSize());

				// if the mouse is over this node
				if (GeneTrees.panel.getMouseX() > xTL && GeneTrees.panel.getMouseX() < xTL + n.getSize() && 
					GeneTrees.panel.getMouseY() > yTL && GeneTrees.panel.getMouseY() < yTL + n.getSize()) {
					g.setColor(Color.BLACK); // highlight it
					g.drawRect(xTL, yTL, n.getSize(), n.getSize());

					// highlight its parent
					if (n.getParent() != null) {
						int xTLparent = n.getParent().getPos()[0] - xScr;
						int yTLparent = n.getParent().getPos()[1] - yScr;
						g.setColor(Color.CYAN);
						g.drawRect(xTLparent, yTLparent, n.getParent().getSize(), n.getParent().getSize());
					}

					// hightlight its children
					for (TreeNode nc : n.getChildren()) {
						int xTLchild = nc.getPos()[0] - xScr;
						int yTLchild  = nc.getPos()[1] - yScr;
						g.setColor(Color.MAGENTA);
						g.drawRect(xTLchild, yTLchild, nc.getSize(), nc.getSize());
					}
				}
			}

			if (GeneTrees.panel.getSimulation().getTrackedTree() == this) {
				g.setColor(Color.ORANGE);
				g.drawRect(xMin - xScr, yMin - yScr, 
						   xMax - xMin, yMax - yMin);
			}
		}
	}

	// gather nutrients through root nodes, and calculate fitness decay
	public void tick() {
		for (TreeNode n : root.getNodes()) {
			// if this is a root node, and it is belowground, gradually increment its fitness
			if (n.getType() == NodeType.Root && 
				n.getPos()[1] + n.getSize() / 2 > GeneTrees.panel.getSimulation().getEnv().getGroundLevel(n.getPos()[0] + n.getSize()/2)) {
				nutrients += (EnvironmentParameters.NODE_ROOT_NUTRIENT_COLLECTION_PER_SIZE * n.getSize());
			}

			// decrement fitness proportional to the size of this node,
			// moreso if it is a structure node
			if (n.getType() == NodeType.Struct) {
				fitness -= n.getSize() * EnvironmentParameters.NODE_STRUCT_FITNESS_DECAY_PER_SIZE;
			} else if (n.isActivated() || n.getType() == NodeType.Root) {
				fitness -= n.getSize() * EnvironmentParameters.NODE_ACTIVE_FITNESS_DECAY_PER_SIZE;
			} else {
				fitness -= n.getSize() * EnvironmentParameters.NODE_INACTIVE_FITNESS_DECAY_PER_SIZE;
			}
		}

		// calculate how much fitness has been gained this tick
		long newFitness = Math.min(energy, nutrients);
		if (newFitness > 0) {
			energy -= newFitness;
			nutrients -= newFitness;
			fitness += EnvironmentParameters.TREE_FITNESS_GAIN_PER_NUTRIENT_AND_POWER * newFitness;
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
		for (TreeNode n : root.getNodes()) {
			System.out.println("pos: " + n.getPos()[0] + "," + n.getPos()[1]);
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
		return root.getNodes().size();
	}

	public ArrayList<TreeNode> getAllNodes() {
		return new ArrayList<TreeNode>(root.getNodes());
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