package xyz.urffer.genetrees2.simulation;

import java.util.HashSet;
import java.util.Random;

public class TreeNode {
	private NodeType type;
	private int size; // diameter in pixels
	private double dist; // distance from parent
	private int angle; // angle (clockwise) from directly below parent
	private int xPos; // xPos of the center
	private int yPos; // yPos of the center
	
	private boolean activated = false; // whether this node has been used (or is vestigial)
	
	private GeneTree owner;
	private TreeNode parent;
	private HashSet<TreeNode> children = new HashSet<TreeNode>();
	
	private Random random;
	
	// creates a blank treenode for I/O
	public TreeNode() {
		// do nothing
	}
	
	// deeply, recursively clones a TreeNode
	public TreeNode(Random random, int x, int y, GeneTree owner, TreeNode parent, TreeNode tgt) {
		this.random = random;
		
		this.size = tgt.getSize();
		this.type = tgt.getType();
		this.parent = parent;
		this.owner = owner;
		this.dist = tgt.getDist();
		this.angle = tgt.getAngle();
		this.xPos = x;
		this.yPos = y;

		for (TreeNode n : tgt.getChildren()) {
			children.add(new TreeNode(random, -1, -1, owner, this, n));
		}
	}
	
	// creates a new TreeNode with given owner and parent node
	public TreeNode(Random random, int x, int y, GeneTree owner, TreeNode parent) {
		this.random = random;
		
		this.size = (int)(random.nextDouble()*9.0) + 20;
		this.type = NodeType.values()[(int)(random.nextDouble()*4.0)];
		this.parent = parent;
		this.owner = owner;
		this.dist = random.nextDouble()*30.0 + 40;
		this.angle = (int)(random.nextDouble()*360.0);
		this.xPos = x;
		this.yPos = y;
		
		// if this is the root node, it must be a structure node
		if (parent == null) {
			this.type = NodeType.Struct;
		} else if (parent.getNumRootChildren() >= 4) {
			// if this node's parent already has more than 4 root nodes, this cannot be a root node
			while (this.type == NodeType.Root)
				this.type = NodeType.values()[(int)(random.nextDouble()*NodeType.values().length)];
		}
		
		// if this happens to become a structure node, there is a %40 chance of getting a child node
		// and a decreasing chance of more
		while (type == NodeType.Struct && random.nextDouble() < 0.40) {
			this.addNewChild();
		}
	}
	
	// recursively mutates this node and its children
	public void mutate() {
		// 10% chance of mutating node type
		if (random.nextDouble() < 0.10) {
			NodeType newType = NodeType.values()[(int)(random.nextDouble()*NodeType.values().length)];
			while (newType != NodeType.Struct && random.nextDouble() < 0.40) // make it more likely that it mutates to a struct node
				newType = NodeType.values()[(int)(random.nextDouble()*NodeType.values().length)];
			
			if (newType == this.type) { // if the type wouldn't change
				// dont do shit
			} else if (newType == NodeType.Struct) {
				// there is a 40% chance of gaining a child if this node changes to structure
				// and a decreasing chance of more children
				while (random.nextDouble() < 0.40) { 
					this.addNewChild();
				}
			} else { // if no longer a struct, remove its children
				this.children.clear();
			}
			
			this.type = newType;
		}
		
		// 20% chance of mutating node size
		if (random.nextDouble() < 0.20) {
			int sizeInc = (int)(random.nextDouble()*16.0) - 16;
			this.size += sizeInc;
			
			if (this.size < 10) {
				this.size = 10;
			}
		}
		
		HashSet<TreeNode> toDelete = new HashSet<TreeNode>();
		for (TreeNode n : children) {
			// 5% chance to lose each child node
			if (random.nextDouble() < .05) {
				toDelete.add(n);
			}
			
			// mutate each child
			n.mutate();
		}
		children.removeAll(toDelete);
		
		// if this is a structure node, there is a 10% chance of adding a child node
		// and a decreasing chance of several child nodes
		while (this.type == NodeType.Struct && random.nextDouble() < 0.10) {
			this.addNewChild();
		}
		
		if (random.nextDouble() < 0.10) { // 10% chance to mutate angle
			int angleInc = (int)(random.nextDouble()*30) - 30; // by up to +/- 15 degs
			angle += angleInc;
		}
		
		if (random.nextDouble() < 0.10) { // 10% chance of changing this node's distance from parent
			double distInc = random.nextDouble()*30.0 - 30;
			dist += distInc;
			
			// dist must be 10 at minimum
			if (dist < 10) {
				dist = 10;
			}
		}
	}
	
	// recursively computes the location within the simulation of this node, its children, grandchildren, etc
	public void initLocation() {
		// if this node is the root node
		if (this.parent == null) {
			owner.setxMin(xPos - size/2);
			owner.setyMin(yPos - size/2);
			owner.setxMax(xPos + size/2);
			owner.setyMax(yPos + size/2);
			
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
			xPos = parent.getXPos();
			yPos = parent.getYPos() - (int)dist;
		} else if (angle < 90) {
			xPos = parent.getXPos() + (int)(Math.sin(angleInRads)*dist);
			yPos = parent.getYPos() - (int)(Math.cos(angleInRads)*dist);
		} else if (angle == 90) {
			xPos = parent.getXPos() + (int)dist;
			yPos = parent.getYPos();
		} else if (angle > 90 && angle < 180) {
			xPos = parent.getXPos() + (int)(Math.cos(angleInRads)*dist);
			yPos = parent.getYPos() + (int)(Math.sin(angleInRads)*dist);
		} else if (angle == 180) {
			xPos = parent.getXPos();
			yPos = parent.getYPos() + (int)dist;
		} else if (angle > 180 && angle < 270) {
			xPos = parent.getXPos() - (int)(Math.sin(angleInRads)*dist);
			yPos = parent.getYPos() + (int)(Math.cos(angleInRads)*dist);
		} else if (angle == 270) {
			xPos = parent.getXPos() - (int)dist;
			yPos = parent.getYPos();
		} else if (angle > 270 && angle < 360) {
			xPos = parent.getXPos() - (int)(Math.cos(angleInRads)*dist);
			yPos = parent.getYPos() - (int)(Math.sin(angleInRads)*dist);
		} else {
			throw new IllegalStateException("illegal angle of: " + angle);
		}
		
		// initialize the location of children
		for (TreeNode n : children) {
			n.initLocation();
		}
		
		if (this.xPos - size/2 < owner.getxMin()) {
			owner.setxMin(xPos - size/2);
		}
		if (this.xPos + size/2 > owner.getxMax()) {
			owner.setxMax(xPos + size/2);
		}
		if (this.yPos - size/2 < owner.getyMin()) {
			owner.setyMin(yPos - size/2);
		}
		if (this.yPos - size/2 > owner.getyMax()) {
			owner.setyMax(yPos + size/2);
		}
	}
	
	// recursively returns this node and all its children, grandchildren, etc
	public HashSet<TreeNode> getNodes() {
		HashSet<TreeNode> ret = new HashSet<TreeNode>();
		
		ret.add(this);
		for (TreeNode t : children) {
			ret.addAll(t.getNodes());
		}
		
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
	
	public int getXPos() {
		return xPos;
	}
	
	public int getYPos() {
		return yPos;
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
		this.xPos = xPos;
	}
	
	public void setYPos(int yPos) {
		this.yPos = yPos;
	}
}