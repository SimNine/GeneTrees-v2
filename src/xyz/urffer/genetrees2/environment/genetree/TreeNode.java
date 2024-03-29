package xyz.urffer.genetrees2.environment.genetree;

import java.util.HashSet;
import java.util.Random;

import xyz.urffer.genetrees2.framework.ParametersLoader;

public class TreeNode {
	private NodeType type;
	private int size; // diameter in pixels
	private double dist; // distance from parent
	private int angle; // angle (clockwise) from directly below parent
	private int[] pos; // position of the top-left corner
	
	private boolean activated = false; // whether this node has been used (or is vestigial)
	
	private GeneTree owner;
	private TreeNode parent;
	private HashSet<TreeNode> children = new HashSet<TreeNode>();
	
	// cached set of all descendants of this node
	private HashSet<TreeNode> descendants = null;
	
	private Random random;
	
	// creates a blank treenode for I/O
	public TreeNode() {
		// do nothing
	}
	
	/**
	 * Deeply, recursively clones a TreeNode.
	 * 
	 * @param random	The random number generator
	 * @param x			x-position of this node; -1 if not root
	 * @param y			y-position of this node; -1 if not root
	 * @param owner		GeneTree that owns this node
	 * @param parent	Parent TreeNode of this TreeNode
	 * @param tgt		TreeNode to be cloned
	 */
	public TreeNode(Random random, int x, int y, GeneTree owner, TreeNode parent, TreeNode tgt) {
		this.random = random;
		this.parent = parent;
		this.owner = owner;
		this.pos = new int[]{x, y};
		
		this.size = tgt.getSize();
		this.type = tgt.getType();
		this.dist = tgt.getDist();
		this.angle = tgt.getAngle();

		for (TreeNode n : tgt.getChildren()) {
			children.add(new TreeNode(random, -1, -1, owner, this, n));
		}
	}
	
	/**
	 * Creates a new TreeNode with given owner and parent node. Used either for creation of an entirely new tree,
	 * or for addition of a new leaf to an existing tree.
	 * 
	 * @param random	The random number generator
	 * @param x			x-position of this node; -1 if not root
	 * @param y			y-position of this node; -1 if not root
	 * @param owner		GeneTree that owns this node
	 * @param parent	Parent TreeNode of this TreeNode
	 */
	public TreeNode(Random random, int x, int y, GeneTree owner, TreeNode parent) {
		this.random = random;
		this.parent = parent;
		this.owner = owner;
		this.pos = new int[]{x, y};
		
		this.size = (int)(random.nextDouble()*9.0) + ParametersLoader.getParams().constantNodeMinimumSize;
		this.type = NodeType.values()[(int)(random.nextDouble()*4.0)];
		this.dist = random.nextDouble()*30.0 + ParametersLoader.getParams().constantNodeMinimumDistance;
		this.angle = (int)(random.nextDouble()*360.0);
		
		// Try to mutate this new node
		this.mutate();
	}
	
	/**
	 *  Recursively mutates this node and its children
	 */
	public void mutate() {
		
		// Chance of mutating this node's type
		if (random.nextDouble() < ParametersLoader.getParams().mutationChanceNodeType) {
			NodeType newType = NodeType.values()[(int)(random.nextDouble()*NodeType.values().length)];
			while (newType != NodeType.Struct && random.nextDouble() < 0.40) // make it more likely that it mutates to a struct node
				newType = NodeType.values()[(int)(random.nextDouble()*NodeType.values().length)];
			
			// Remove children if this is no longer a struct node
			if (newType != NodeType.Struct) {
				this.children.clear();
			}
			
			this.type = newType;
		}
		
		// Chance of mutating this node's size
		if (random.nextDouble() < ParametersLoader.getParams().mutationChanceNodeSize) {
			int sizeInc = (int)(random.nextDouble()*16.0) - 16;
			this.size += sizeInc;
			
			if (this.size < ParametersLoader.getParams().constantNodeMinimumSize) {
				this.size = ParametersLoader.getParams().constantNodeMinimumSize;
			}
		}
		
		// Chance to lose each child node, then recursively mutate each child
		HashSet<TreeNode> toDelete = new HashSet<TreeNode>();
		for (TreeNode n : children) {
			if (random.nextDouble() < ParametersLoader.getParams().mutationChanceNodeDeletion) {
				toDelete.add(n);
			}
			
			// mutate each child
			n.mutate();
		}
		children.removeAll(toDelete);
		
		// Chance of adding child nodes if this is a structure node
		while (this.type == NodeType.Struct && random.nextDouble() < ParametersLoader.getParams().mutationChanceNodeAddition) {
			this.addNewChild();
		}
		
		// Chance to mutate angle between this node and its parent
		if (random.nextDouble() < ParametersLoader.getParams().mutationChanceNodeAngle) { 
			int angleInc = (int)(random.nextDouble()*30) - 30; // by up to +/- 15 degs
			angle += angleInc;
		}
		
		// Chance of changing this node's distance from its parent
		if (random.nextDouble() < ParametersLoader.getParams().mutationChanceNodeDistance) { 
			double distInc = random.nextDouble()*30.0 - 30;
			dist += distInc;
			
			// Check for minimum distance
			if (dist < ParametersLoader.getParams().constantNodeMinimumDistance) {
				dist = ParametersLoader.getParams().constantNodeMinimumDistance;
			}
		}
	}
	
	/**
	 * Recursively computes the location within the simulation of this node and its descendants.
	 */
	public void initLocation() {
		// if this node is the root node
		if (this.parent == null) {
			owner.setxMin(pos[0]);
			owner.setyMin(pos[1]);
			owner.setxMax(pos[0] + size);
			owner.setyMax(pos[1] + size);
			
			// initialize the location of children
			for (TreeNode n : children) {
				n.initLocation();
			}
			return;
		}
		
		// fix owner (if not fixed)
		owner = parent.getOwner();
		
		// angle correction
		while (angle > 360) {
			angle -= 360;
		}
		while (angle < 0) {
			angle += 360;
		}
		
		// computing the absolute location of this node
		int tempAngle = angle;
		while (tempAngle > 90) {
			tempAngle -= 90;
		}
		double angleInRads = Math.toRadians(tempAngle);
		angleInRads = Math.abs(angleInRads);
		
		// REMEMBER HERE
		// "angle" is the angle from THIS NODE TO ITS PARENT
		// NOT THE OTHER WAY AROUND
		// (hence the wonky sin and cos)
		if (angle == 0) {
			pos[0] = parent.getPos()[0];
			pos[1] = parent.getPos()[1] - (int)dist;
		} else if (angle < 90) {
			pos[0] = parent.getPos()[0] + (int)(Math.sin(angleInRads)*dist);
			pos[1] = parent.getPos()[1] - (int)(Math.cos(angleInRads)*dist);
		} else if (angle == 90) {
			pos[0] = parent.getPos()[0] + (int)dist;
			pos[1] = parent.getPos()[1];
		} else if (angle > 90 && angle < 180) {
			pos[0] = parent.getPos()[0] + (int)(Math.cos(angleInRads)*dist);
			pos[1] = parent.getPos()[1] + (int)(Math.sin(angleInRads)*dist);
		} else if (angle == 180) {
			pos[0] = parent.getPos()[0];
			pos[1] = parent.getPos()[1] + (int)dist;
		} else if (angle > 180 && angle < 270) {
			pos[0] = parent.getPos()[0] - (int)(Math.sin(angleInRads)*dist);
			pos[1] = parent.getPos()[1] + (int)(Math.cos(angleInRads)*dist);
		} else if (angle == 270) {
			pos[0] = parent.getPos()[0] - (int)dist;
			pos[1] = parent.getPos()[1];
		} else if (angle > 270 && angle < 360) {
			pos[0] = parent.getPos()[0] - (int)(Math.cos(angleInRads)*dist);
			pos[1] = parent.getPos()[1] - (int)(Math.sin(angleInRads)*dist);
		} else {
			throw new IllegalStateException("illegal angle of: " + angle);
		}
		
		// initialize the location of children
		for (TreeNode n : children) {
			n.initLocation();
		}
		
		if (this.pos[0] < owner.getxMin()) {
			owner.setxMin(this.pos[0]);
		}
		if (this.pos[0] + size > owner.getxMax()) {
			owner.setxMax(this.pos[0] + size);
		}
		if (this.pos[1] < owner.getyMin()) {
			owner.setyMin(this.pos[1]);
		}
		if (this.pos[1] + size > owner.getyMax()) {
			owner.setyMax(this.pos[1] + size);
		}
	}
	
	/**
	 * Recursively returns this node and all its children, grandchildren, etc
	 * 
	 * @return	A set containing this node and all its descendents
	 */
	public HashSet<TreeNode> getDescendants() {
		if (this.descendants != null) {
			return this.descendants;
		}
		
		HashSet<TreeNode> ret = new HashSet<TreeNode>();
		
		ret.add(this);
		for (TreeNode t : children) {
			ret.addAll(t.getDescendants());
		}
		
		this.descendants = ret;
		return ret;
	}
	
	// returns just this node's children
	public HashSet<TreeNode> getChildren() {
		return children;
	}
	
	// returns this node's parent node
	public TreeNode getParent() {
		return parent;
	}
	
	// returns this node's GeneTree
	public GeneTree getOwner() {
		return owner;
	}
	
	public int getSize() {
		return size;
	}
	
	public NodeType getType() {
		return type;
	}
	
	public double getDist() {
		return dist;
	}
	
	public int getAngle() {
		return angle;
	}
	
	public int[] getPos() {
		return pos;
	}
	
	public void addNewChild() {
		children.add(new TreeNode(random, -1, -1, owner, this));
	}
	
	public void addChild(TreeNode node) {
		children.add(node);
	}
	
	public void setParent(TreeNode p) {
		this.parent = p;
	}
	
	public void setType(NodeType t) {
		this.type = t;
	}
	
	public void setSize(int i) {
		this.size = i;
	}
	
	public void setAngle(int a) {
		this.angle = a;
	}
	
	public void setDistance(double d) {
		this.dist = d;
	}
	
	public void setOwner(GeneTree t) {
		this.owner = t;
	}
	
	public int getNumRootChildren() {
		int ret = 0;
		for (TreeNode n : children) {
			if (n.getType() == NodeType.Root) {
				ret++;
			}
		}
		return ret;
	}
	
	public boolean isActivated() {
		return activated;
	}
	
	public void setActivated(boolean b) {
		activated = b;
	}
	
	public void setXPos(int xPos) {
		this.pos[0] = xPos;
	}
	
	public void setYPos(int yPos) {
		this.pos[1] = yPos;
	}
}