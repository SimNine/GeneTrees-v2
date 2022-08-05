package xyz.urffer.genetrees2.environment;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.util.HashSet;
import java.util.Random;

import xyz.urffer.genetrees2.environment.genetree.GeneTree;

public class Environment {
	private HashSet<GeneTree> trees = new HashSet<GeneTree>();
	private HashSet<SunSpeck> sun = new HashSet<SunSpeck>();
	private HashSet<GeneSeed> seeds = new HashSet<GeneSeed>();
	private HashSet<RainDrop> rain = new HashSet<RainDrop>();
	
	private BufferedImage groundImg;
	private int groundBaseline;
	private int groundDegree;
	private double[] groundFreq;
	private double[] groundAmp;
	private double[] groundDisp;
	
	private int envWidth;
	private int envHeight;
	
	private Random random;
	
	// build an environment
	public Environment(Random random, int sWidth, int sHeight, 
					   int gBaseline, double[] gFreq, double[] gAmp,
					   double[] gDisp) {
		this.random = random;
		
		groundBaseline = gBaseline;
		groundFreq = gFreq;
		groundAmp = gAmp;
		groundDisp = gDisp;
		
		int min = Math.min(groundFreq.length, groundAmp.length);
		min = Math.min(min, groundDisp.length);
		groundDegree = min;
		
		envWidth = sWidth;
		envHeight = sHeight;
		
		// create the background image
		groundImg = new BufferedImage(envWidth, envHeight, BufferedImage.TYPE_4BYTE_ABGR);
		for (int x = 0; x < groundImg.getWidth(); x++) {
			double m = getGroundLevel(x);
			
			for (int y = (int)m; y < groundImg.getHeight(); y++) {
				groundImg.setRGB(x, y, new Color(183, 85, 23).getRGB());
			}
		}
		
		// populate the list of trees
		for (int i = 0; i < EnvironmentParameters.ENVIRONMENT_SPONTANEOUS_TREES_PER_GENERATION; i++) {
			double xPos = random.nextDouble()*envWidth;
			trees.add(new GeneTree(random, (int)(xPos), (int)getGroundLevel(xPos)));
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
	
	public double getGroundLevel(double xPos) {
		double sum = 0;
		for (int i = 0; i < groundDegree; i++) {
			sum += Math.cos(groundFreq[i]*xPos + groundDisp[i])*groundAmp[i];
		}
		sum += groundBaseline;
		return sum;
	}
	
	public BufferedImage getGroundImg() {
		return groundImg;
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
	
	public double[] getGroundFreq() {
		return groundFreq;
	}
	
	public double[] getGroundAmp() {
		return groundAmp;
	}
	
	public double[] getGroundDisp() {
		return groundDisp;
	}
	
	public int getGroundBaseline() {
		return groundBaseline;
	}
	
	public int getGroundDegree() {
		return groundDegree;
	}
}
