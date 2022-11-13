package xyz.urffer.genetrees2.environment;

import java.util.HashSet;
import java.util.Random;

import xyz.urffer.genetrees2.environment.genetree.GeneTree;
import xyz.urffer.genetrees2.framework.ParametersLoader;

public class Environment {
	private HashSet<GeneTree> trees = new HashSet<GeneTree>();
	private HashSet<SunSpeck> sun = new HashSet<SunSpeck>();
	private HashSet<GeneSeed> seeds = new HashSet<GeneSeed>();
	private HashSet<RainDrop> rain = new HashSet<RainDrop>();
	
	private int envWidth;
	private int envHeight;
	
	private Random random;
	
	private Landscape landscape;
	
	// build an environment
	public Environment(
		Random random,
		int sWidth,
		int sHeight, 
		int gBaseline,
		double[] gFreq,
		double[] gAmp,
		double[] gDisp
	) {
		this.random = random;
		
		this.landscape = new Landscape(
			sWidth,
			sHeight,
			gBaseline,
			gFreq,
			gAmp,
			gDisp
		);
		
		envWidth = sWidth;
		envHeight = sHeight;
		
		// populate the list of trees
		for (int i = 0; i < ParametersLoader.getParams().envSpontaneousTreesPerGen; i++) {
			double xPos = random.nextDouble()*envWidth;
			trees.add(new GeneTree(random, (int)(xPos), landscape.getGroundLevel(xPos)));
		}
	}
	
	// gets a tree at a position
	public GeneTree getTreeAt(int x, int y) {
		for (GeneTree t : trees) {
			if (t.isOverTree(x, y)) {
				return t;
			}
		}
		return null;
	}
	
	public HashSet<GeneTree> getTrees() {
		return trees;
	}
	
	public void setTrees(HashSet<GeneTree> t) {
		trees = t;
	}
	
	public HashSet<SunSpeck> getSun() {
		return sun;
	}
	
	public HashSet<RainDrop> getRain() {
		return rain;
	}
	
	public HashSet<GeneSeed> getSeeds() {
		return seeds;
	}
	
	public int getEnvWidth() {
		return envWidth;
	}

	public int getEnvHeight() {
		return envHeight;
	}
	
	public Landscape getLandscape() {
		return landscape;
	}
}
