package framework;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.HashSet;

import javax.swing.JOptionPane;

import simulation.GeneTree;
import simulation.TreeNode;

public class Loader {
	
	private static File saveDir = new File("savedgames");
	
	public static void saveGame() {
		GeneTrees.panel.stopTime();
		
        if (!saveDir.exists()) saveDir.mkdir();
        
        String filename = JOptionPane.showInputDialog(GeneTrees.panel, "Save Level", null);
        
        saveGame(filename);
	}

	public static void saveGame(String saveName) {
		GeneTrees.panel.stopTime();
		
        if (!saveDir.exists()) saveDir.mkdir();
        
        String filename = saveName + ".gt";
        if (filename.equals("null.gt")) {
        	GeneTrees.panel.startTime();
        	return;
        }
        
        File save = new File(saveDir, filename);
        
        if (save.exists()) {
            System.out.print("file already exists. overwriting...");
            save.delete();
            System.out.println("overwritten");
        }
        
        // begin saving process
        try {
            System.out.print("saving " + filename + "...");
            save.createNewFile();
            ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(save));
            
            // get all the genetrees
            ArrayList<GeneTree> trees = GeneTrees.panel.getTrees();
            
            // write number of trees
            oos.writeInt(trees.size());
            
            // for each tree
            for (int i = 0; i < trees.size(); i++) {
            	// get current tree and its nodes
            	GeneTree curr = trees.get(i);
            	ArrayList<TreeNode> nodes = curr.getAllNodes();
            	
                // write number of nodes in current tree
                oos.writeInt(curr.getNumNodes());
                // write age of current tree
                oos.writeInt(curr.getAge());
                // write origin gen of current tree
                oos.writeInt(curr.getOrigin());
                
                // for each node
                for (int j = 0; j < nodes.size(); j++) {
                	// get the current node
                	TreeNode node = nodes.get(j);
                	
                	// write its type, size, angle, and distance
                	oos.writeInt(node.getType());
                	oos.writeInt(node.getSize());
                	oos.writeInt(node.getAngle());
                	oos.writeDouble(node.getDist());
                	
                	// get its parent
                	TreeNode parent = node.getParent();
                	if (parent == null) {
                		// if this is the root, write a dummy value
                		oos.writeInt(-1);
                	} else {
	                	// find the index of its parent
	                	int parentIndex = nodes.indexOf(parent);
	                	// write the index of its parent
	                	oos.writeInt(parentIndex);
                	}
                	
                	// get its children
                	ArrayList<TreeNode> children = new ArrayList<TreeNode>(node.getChildren());
                	// write the number of children
                	oos.writeInt(children.size());
                	// for each child
                	for (int k = 0; k < children.size(); k++) {
                		// find the index of this child
                		int childIndex = nodes.indexOf(children.get(k));
                		// write the index of this child
                		oos.writeInt(childIndex);
                	}
                }
            }
            
            // write the number of this generation
            oos.writeInt(GeneTrees.panel.getCurrGen());
            
            // done, finish up
            oos.flush();
            oos.close();
            System.out.println("saved");
        } catch (Exception e) {
            System.out.println("failed");
            e.printStackTrace();
            System.exit(1);
        }
        
        GeneTrees.panel.startTime();
    }
    
    public static void loadGame() {
    	GeneTrees.panel.stopTime();
    	
        String filename = JOptionPane.showInputDialog(null, "load a generation", null) + ".gt";
        if (filename.equals("null.gt")) {
        	GeneTrees.panel.startTime();
        	return;
        }
        
        // begin gameloading process
        try {
            System.out.print("loading " + filename + "... ");
            File file = new File(saveDir, filename);
            ObjectInputStream ois = new ObjectInputStream(new FileInputStream(file));
            
            // read number of trees
            int numTrees = ois.readInt();
            
            // initialize arraylist of trees
            ArrayList<GeneTree> trees = new ArrayList<GeneTree>(numTrees);
            
            // for each tree
            for (int i = 0; i < numTrees; i++) {
            	// get number of nodes in the tree
            	int numNodes = ois.readInt();
            	// get age of current tree
            	int age = ois.readInt();
            	// get origin gen of current tree
            	int origin = ois.readInt();
            	
            	// initialize list of nodes
            	ArrayList<TreeNode> nodes = new ArrayList<TreeNode>(numNodes);
            	// initialize list of indices of nodes' parents
            	ArrayList<Integer> parentIndices = new ArrayList<Integer>(numNodes);
            	// initialize list of sets of indices of nodes' children
            	ArrayList<HashSet<Integer>> childrenIndices = new ArrayList<HashSet<Integer>>(numNodes);
            	
            	// for each node
            	for (int j = 0; j < numNodes; j++) {
            		// initialize a new node
            		TreeNode newNode = new TreeNode();
            		
            		// read and set its type, size, angle, and distance
            		newNode.setType(ois.readInt());
            		newNode.setSize(ois.readInt());
            		newNode.setAngle(ois.readInt());
            		newNode.setDistance(ois.readDouble());
            		
            		// read the index of its parent and add it for later processing
            		parentIndices.add(j, ois.readInt());
            		
            		// read the number of children of this node
            		int numChildren = ois.readInt();
            		// initialize a set of the indices of this node's children
            		HashSet<Integer> chilIndices = new HashSet<Integer>(numChildren);
            		
            		// for each child
            		for (int k = 0; k < numChildren; k++) {
            			// add the index of this child for later processing
            			chilIndices.add(ois.readInt());
            		}
            		
            		// add this node's set of children indices to the rest
        			childrenIndices.add(j, chilIndices);
            		
            		// add this node (without a parent or children) to the list of nodes
            		nodes.add(j, newNode);
            	}
            	
            	// initialize an empty root node
            	TreeNode root = null;
            	
            	/*
            	 *  at this point, all data besides the generation number has been read
            	 * 
            	 *  now that all raw nodes are added to the list of nodes,
            	 *  it is necessary to link nodes with their parents and children.
            	 *  for each node:
            	 */
            	for (int j = 0; j < numNodes; j++) {
            		// get the current node
            		TreeNode currNode = nodes.get(j);
            		
            		// find the index of this node's parent
            		int parentIndex = parentIndices.get(j);
            		// if this node's parent index has the dummy value of -1, it is the root
            		if (parentIndex == -1) {
            			currNode.setParent(null);
            			root = currNode;
            		} else {
                		currNode.setParent(nodes.get(parentIndex));
            		}
            		
            		// get the set of this node's childrens' indices
            		HashSet<Integer> childIndices = childrenIndices.get(j);
            		
            		// find this node's children and link them
            		for (Integer in : childIndices) {
            			TreeNode child = nodes.get(in);
            			currNode.addChild(child);
            		}
            	}
            	
            	// create a new geneTree
            	GeneTree newTree = new GeneTree(root);
            	
            	// set newTree's attributes
            	newTree.setAge(age);
            	newTree.setOrigin(origin);
            	
            	// once more, iterate through each node to set their owner tree
            	for (int j = 0; j < numNodes; j++) {
            		nodes.get(j).setOwner(newTree);
            	}
            	
            	// finally, add this tree to the list of trees
            	trees.add(i, newTree);
            }
            
            // now, set the list of trees for the simulation
            GeneTrees.panel.reset(trees, ois.readInt());
            
            // finish up
            ois.close();
            System.out.println("done");
        } catch (Exception e) {
            System.out.println("failed");
            e.printStackTrace();
            System.exit(1);
        }
        
        GeneTrees.panel.startTime();
    }
}