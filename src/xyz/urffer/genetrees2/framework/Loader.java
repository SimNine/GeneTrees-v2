package xyz.urffer.genetrees2.framework;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedList;

import javax.swing.JOptionPane;

import xyz.urffer.urfutils.Pair;
import xyz.urffer.urfutils.grapher.*;

import xyz.urffer.genetrees2.simulation.*;

public class Loader {
	
	private static File saveDir = new File("saves");
	private static final int SAVEFILE_VERSION = 1;
	
	public static void saveGame() {
		GeneTrees.panel.getSimulation().stopTime();
		
        if (!saveDir.exists()) saveDir.mkdir();
        
        String filename = JOptionPane.showInputDialog(GeneTrees.panel, "Save", null);
        
        saveGame(filename);
	}

	public static void saveGame(String saveName) {
		GeneTrees.panel.getSimulation().stopTime();
		
        if (!saveDir.exists()) saveDir.mkdir();
        
        String filename = saveName + ".gt2";
        if (filename.equals("null.gt2")) {
        	GeneTrees.panel.getSimulation().startTime();
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
            
            // save the file version
            oos.writeInt(SAVEFILE_VERSION);
            
            // save all plot points
            savePlot(oos, GeneTrees.fitnessPanel.getDataset(GeneTrees.GRAPHDATA_FITNESS_MAX));
            savePlot(oos, GeneTrees.fitnessPanel.getDataset(GeneTrees.GRAPHDATA_FITNESS_AVG));
            savePlot(oos, GeneTrees.fitnessPanel.getDataset(GeneTrees.GRAPHDATA_FITNESS_MIN));
            savePlot(oos, GeneTrees.populationPanel.getDataset(GeneTrees.GRAPHDATA_POPULATION));
            savePlot(oos, GeneTrees.nodeStatPanel.getDataset(GeneTrees.GRAPHDATA_NODES_ALL));
            savePlot(oos, GeneTrees.nodeStatPanel.getDataset(GeneTrees.GRAPHDATA_NODES_LEAF));
            savePlot(oos, GeneTrees.nodeStatPanel.getDataset(GeneTrees.GRAPHDATA_NODES_RAINCATCHER));
            savePlot(oos, GeneTrees.nodeStatPanel.getDataset(GeneTrees.GRAPHDATA_NODES_ROOT));
            savePlot(oos, GeneTrees.nodeStatPanel.getDataset(GeneTrees.GRAPHDATA_NODES_STRUCTURE));
            savePlot(oos, GeneTrees.nodeStatPanel.getDataset(GeneTrees.GRAPHDATA_NODES_SEEDDROPPER));
            savePlot(oos, GeneTrees.particleStatPanel.getDataset(GeneTrees.GRAPHDATA_PARTICLES_SUNDROPS));
            savePlot(oos, GeneTrees.particleStatPanel.getDataset(GeneTrees.GRAPHDATA_PARTICLES_RAINDROPS));
            savePlot(oos, GeneTrees.particleStatPanel.getDataset(GeneTrees.GRAPHDATA_PARTICLES_SEEDS));
            
            // get all the genetrees
            HashSet<GeneTree> trees = GeneTrees.panel.getSimulation().getEnv().getTrees();
            
            // write number of trees
            oos.writeInt(trees.size());
            
            // for each tree
            for (xyz.urffer.genetrees2.simulation.GeneTree t : trees) {
            	// get current tree and its nodes
            	GeneTree curr = t;
            	ArrayList<TreeNode> nodes = curr.getAllNodes();
            	
                // write number of nodes in current tree
                oos.writeInt(curr.getNumNodes());
                // write age of current tree
                oos.writeInt(curr.getAge());
                
                // write the position of this tree's root
                oos.writeInt(curr.getRoot().getXPos());
                oos.writeInt(curr.getRoot().getYPos());
                
                // for each node
                for (TreeNode n : nodes) {
                	TreeNode node = n;
                	
                	// write its type, size, angle, and distance
                	oos.writeInt(NodeType.toInt(node.getType()));
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
            
            // write all environment specific info
            Environment env = GeneTrees.panel.getSimulation().getEnv();
            oos.writeInt(GeneTrees.panel.getSimulation().getNumGens());
            oos.writeInt(env.getEnvWidth());
            oos.writeInt(env.getEnvHeight());
            oos.writeInt(env.getGroundBaseline());
            oos.writeInt(env.getGroundDegree());
            for (int i = 0; i < env.getGroundDegree(); i++) {
            	oos.writeDouble(env.getGroundFreq()[i]);
            	oos.writeDouble(env.getGroundAmp()[i]);
            	oos.writeDouble(env.getGroundDisp()[i]);
            }
            
            // done, finish up
            oos.flush();
            oos.close();
            System.out.println("saved");
        } catch (Exception e) {
            System.out.println("failed");
            e.printStackTrace();
            System.exit(1);
        }
        
        GeneTrees.panel.getSimulation().startTime();
    }
	
	private static void savePlot(ObjectOutputStream oos, GraphDataset gSet) throws IOException {
		LinkedList<Pair<Double, Double>> set = gSet.getPoints();
		oos.writeInt(set.size());
        for (Pair<Double, Double> p : set) {
        	oos.writeDouble(p.a);
        	oos.writeDouble(p.b);
        }
	}
	
	private static void readPlot(ObjectInputStream ois, GraphDataset set) throws IOException {
		int numPoints = ois.readInt();
		for (int i = 0; i < numPoints; i++) {
			set.addPoint(ois.readDouble(), ois.readDouble());
		}
	}
    
    public static void loadGame() {
    	GeneTrees.panel.getSimulation().stopTime();
    	
        String filename = JOptionPane.showInputDialog(null, "load a generation", null) + ".gt2";
        if (filename.equals("null.gt2")) {
        	GeneTrees.panel.getSimulation().startTime();
        	return;
        }
        
        // begin gameloading process
        try {
            System.out.print("loading " + filename + "... ");
            File file = new File(saveDir, filename);
            ObjectInputStream ois = new ObjectInputStream(new FileInputStream(file));
            
            // load the savefile version
            //int savefile_version = ois.readInt();
            
            // load all plot points
            readPlot(ois, GeneTrees.fitnessPanel.getDataset(GeneTrees.GRAPHDATA_FITNESS_MAX));
            readPlot(ois, GeneTrees.fitnessPanel.getDataset(GeneTrees.GRAPHDATA_FITNESS_AVG));
            readPlot(ois, GeneTrees.fitnessPanel.getDataset(GeneTrees.GRAPHDATA_FITNESS_MIN));
            readPlot(ois, GeneTrees.populationPanel.getDataset(GeneTrees.GRAPHDATA_POPULATION));
            readPlot(ois, GeneTrees.nodeStatPanel.getDataset(GeneTrees.GRAPHDATA_NODES_ALL));
            readPlot(ois, GeneTrees.nodeStatPanel.getDataset(GeneTrees.GRAPHDATA_NODES_LEAF));
            readPlot(ois, GeneTrees.nodeStatPanel.getDataset(GeneTrees.GRAPHDATA_NODES_RAINCATCHER));
            readPlot(ois, GeneTrees.nodeStatPanel.getDataset(GeneTrees.GRAPHDATA_NODES_ROOT));
            readPlot(ois, GeneTrees.nodeStatPanel.getDataset(GeneTrees.GRAPHDATA_NODES_STRUCTURE));
            readPlot(ois, GeneTrees.nodeStatPanel.getDataset(GeneTrees.GRAPHDATA_NODES_SEEDDROPPER));
            readPlot(ois, GeneTrees.particleStatPanel.getDataset(GeneTrees.GRAPHDATA_PARTICLES_SUNDROPS));
            readPlot(ois, GeneTrees.particleStatPanel.getDataset(GeneTrees.GRAPHDATA_PARTICLES_RAINDROPS));
            readPlot(ois, GeneTrees.particleStatPanel.getDataset(GeneTrees.GRAPHDATA_PARTICLES_SEEDS));
            
            // read number of trees
            int numTrees = ois.readInt();
            
            // initialize arraylist of trees
            ArrayList<GeneTree> trees = new ArrayList<GeneTree>(numTrees);
            
            // for each tree
            for (int i = 0; i < numTrees; i++) {
            	int numNodes = ois.readInt();
            	int age = ois.readInt();
            	
            	int xPos = ois.readInt();
            	int yPos = ois.readInt();
            	
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
            		newNode.setType(NodeType.toType(ois.readInt()));
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
            			currNode.setXPos(xPos);
            			currNode.setYPos(yPos);
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
            	
            	// once more, iterate through each node to set their owner tree
            	for (int j = 0; j < numNodes; j++) {
            		nodes.get(j).setOwner(newTree);
            	}
            	
            	// finally, add this tree to the list of trees
            	trees.add(i, newTree);
            }
            
            // rebuild the environment
            int eGens = ois.readInt();
            int eWidth = ois.readInt();
            int eHeight = ois.readInt();
            int gBaseline = ois.readInt();
            int gDegree = ois.readInt();
            ArrayList<Double> gFreq = new ArrayList<Double>();
            ArrayList<Double> gAmp = new ArrayList<Double>();
            ArrayList<Double> gDisp = new ArrayList<Double>();
            for (int i = 0; i < gDegree; i++) {
            	gFreq.add(ois.readDouble());
            	gAmp.add(ois.readDouble());
            	gDisp.add(ois.readDouble());
            }
            double[] gFreqs = new double[gFreq.size()];
            double[] gAmps = new double[gAmp.size()];
            double[] gDisps = new double[gDisp.size()];
            for (int i = 0; i < gDegree; i++) {
            	gFreqs[i] = gFreq.get(i);
            	gAmps[i] = gAmp.get(i);
            	gDisps[i] = gDisp.get(i);
            }
            GeneTrees.panel.getSimulation().setEnv(new Environment(eWidth, eHeight, gBaseline, gFreqs, gAmps, gDisps));
            HashSet<GeneTree> treeSet = new HashSet<GeneTree>();
            for (GeneTree t : trees) {
            	treeSet.add(t);
            }
            GeneTrees.panel.getSimulation().getEnv().setTrees(treeSet);
            GeneTrees.panel.getSimulation().setNumGens(eGens);
            
            // finish up
            ois.close();
            System.out.println("done");
        } catch (Exception e) {
            System.out.println("failed");
            e.printStackTrace();
            System.exit(1);
        }
        
        GeneTrees.panel.getSimulation().startTime();
    }
}