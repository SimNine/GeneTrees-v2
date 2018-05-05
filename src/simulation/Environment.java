package simulation;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;

import framework.GeneTrees;

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
	
	private int simWidth;
	private int simHeight;

	private long minFitness = 0;
	private long maxFitness = 0;
	private long avgFitness = 0;
	private long minAlltime = Long.MAX_VALUE;
	private long maxAlltime = Long.MIN_VALUE;
	private long maxTreesAlltime = Long.MIN_VALUE;

	private long tickCount = 0;
	private int ticksThisSec = 0;
	private long prevTime = System.currentTimeMillis();
	private int numGens = 0;
	
	public boolean multithreading = false;
	
	// build an environment
	public Environment(int sWidth, int sHeight, int gBaseline, double[] gFreq, double[] gAmp, double[] gDisp) {
		groundBaseline = gBaseline;
		groundFreq = gFreq;
		groundAmp = gAmp;
		groundDisp = gDisp;
		
		int min = Math.min(groundFreq.length, groundAmp.length);
		min = Math.min(min, groundDisp.length);
		groundDegree = min;
		
		simWidth = sWidth;
		simHeight = sHeight;
		
		// create the background image
		groundImg = new BufferedImage(simWidth, simHeight, BufferedImage.TYPE_4BYTE_ABGR);
		for (int x = 0; x < groundImg.getWidth(); x++) {
			double m = getGroundLevel(x);
			
			for (int y = (int)m; y < groundImg.getHeight(); y++) {
				groundImg.setRGB(x, y, new Color(183, 85, 23).getRGB());
			}
		}
		
		// warm up the environment
		warmup(1000);
		
		// populate the list of trees
		for (int i = 0; i < 100; i++) {
			double xPos = Math.random()*simWidth;
			trees.add(new GeneTree((int)(xPos), (int)getGroundLevel(xPos)));
		}
	}
	
	// simulate weather particle production for some arbitrary number of ticks
	public void warmup(int numTicks) {
		for (int i = 0; i < numTicks; i++) {
			// add new sunspecks
			for (int j = 0; j < 1; j++) {
				double pct = Math.random();
				pct = pct * pct;
				sun.add(new SunSpeck((int)(pct * (double)simWidth), 0));
			}
			
			// check collision of sunspecks with ground
			HashSet<SunSpeck> remSun = new HashSet<SunSpeck>();
			for (SunSpeck s : sun) { // for each sunspeck
				s.tick(); // tick each sunspeck
				if (s.getYPos() > getGroundLevel(s.getXPos())) { // check if sunspeck has hit ground
					remSun.add(s);
				}
			}
			sun.removeAll(remSun); // remove all specks that have hit the ground
			
			// add new raindrops
			for (int j = 0; j < 1; j++) {
				double pct = Math.random();
				pct = 1.0 - (pct * pct);
				rain.add(new RainDrop((int)(pct * (double)simWidth), 0));
			}
			
			// check collision of raindrops with ground
			HashSet<RainDrop> remRain = new HashSet<RainDrop>();
			for (RainDrop d : rain) { // for each raindrop
				d.tick(); // tick each raindrop
				if (d.getYPos() > getGroundLevel(d.getXPos())) { // check if raindrop has hit ground
					remRain.add(d);
				}
			}
			rain.removeAll(remRain); // remove all drops that have hit the ground
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
	
	// computes the collision of all sunspecks with the ground and with trees
	public void computeSun() {
		// add new sunspecks
		for (int i = 0; i < 2; i++) {
			double pct = Math.random();
			pct = pct * pct;
			sun.add(new SunSpeck((int)(pct * (double)simWidth), 0));
		}
		
		// check collision of sunspecks with ground
		HashSet<SunSpeck> remSun = new HashSet<SunSpeck>();
		for (SunSpeck s : sun) { // for each sunspeck
			s.tick(); // tick each sunspeck
			if (s.getYPos() > getGroundLevel(s.getXPos())) { // check if sunspeck has hit ground
				remSun.add(s);
			}
		}
		sun.removeAll(remSun); // remove all specks that have hit the ground
		
		// check collision of sunspecks with each tree
		for (GeneTree t : trees) {
			for (TreeNode n : t.getAllNodes()) {
				HashSet<SunSpeck> rem = new HashSet<SunSpeck>();
				for (SunSpeck ss : sun) {
					int sx = ss.getXPos();
					int sy = ss.getYPos();
					int nx = n.getXPos();
					int ny = n.getYPos();
					int nd = n.getSize() / 2;

					// if the sunspeck hits this node, remove it
					if ((nx - sx) * (nx - sx) + (ny - sy) * (ny - sy) < nd * nd) {
						rem.add(ss);

						// if this node is a leaf, increment its fitness
						if (n.getType() == NodeType.Leaf) {
							n.setActivated(true);
							t.setEnergy(t.getEnergy() + ss.getPower());
						}
					}
				}
				sun.removeAll(rem);
			}
		}
	}
	
	// check collision of raindrops with ground and with trees
	public void computeRain() {
		// add new raindrops
		for (int i = 0; i < 2; i++) {
			double pct = Math.random();
			pct = 1.0 - (pct * pct);
			rain.add(new RainDrop((int)(pct * (double)simWidth), 0));
		}
		
		// check collision of raindrops with ground
		HashSet<RainDrop> remRain = new HashSet<RainDrop>();
		for (RainDrop d : rain) { // for each raindrop
			d.tick(); // tick each raindrop
			if (d.getYPos() > getGroundLevel(d.getXPos())) { // check if raindrop has hit ground
				remRain.add(d);
			}
		}
		rain.removeAll(remRain); // remove all drops that have hit the ground
		
		// check collision of raindrops with each tree
		for (GeneTree t : trees) {
			for (TreeNode n : t.getAllNodes()) {
				if (n.getType() != NodeType.Raincatcher) {
					continue;
				}
				
				HashSet<RainDrop> rem = new HashSet<RainDrop>();
				for (RainDrop rd : rain) {
					int rx = rd.getXPos();
					int ry = rd.getYPos();
					int nx = n.getXPos();
					int ny = n.getYPos();
					int nd = n.getSize() / 2;

					// if the raindrop hits this node, remove it
					if ((nx - rx) * (nx - rx) + (ny - ry) * (ny - ry) < nd * nd) {
						n.setActivated(true);
						rem.add(rd);
						t.setEnergy(t.getEnergy() + rd.getPower());
					}
				}
				rain.removeAll(rem);
			}
		}
	}
	
	// computes the fitness of each tree
	public void computeFitness() {		
		// recompute fitness of each tree
		long totalFitness = 0;
		maxFitness = Integer.MIN_VALUE;
		minFitness = Integer.MAX_VALUE;
		for (GeneTree t : trees) {
			long thisFitness = t.getFitness();
			totalFitness += thisFitness;
			if (thisFitness > maxFitness) maxFitness = thisFitness;
			if (thisFitness < minFitness) minFitness = thisFitness;
		}
		avgFitness = totalFitness/trees.size();
		
		// evaluate each tree's chance to reproduce
		List<GeneTree> treesSorted = new ArrayList<GeneTree>(trees);
		Collections.sort(treesSorted);
		for (int i = 0; i < treesSorted.size(); i++) {
			treesSorted.get(i).setFitnessPercentage((double)i / (double)treesSorted.size());
		}
	}
	
	public void tick() {
		long currTime = System.currentTimeMillis();
		if (currTime - prevTime > 1000) {
			if (GeneTrees.debug)
				System.out.println("ticks per sec: " + ticksThisSec + " - multithreading: " + multithreading);
			ticksThisSec = 0;
			prevTime = currTime;
		}
		
		Thread sunChecker = new Thread() {
			public void run() {
				GeneTrees.panel.getEnv().computeSun();
			}
		};
		
		Thread rainChecker = new Thread() {
			public void run() {
				GeneTrees.panel.getEnv().computeRain();
			}
		};
		
		Thread fitnessChecker = new Thread() {
			public void run() {
				GeneTrees.panel.getEnv().computeFitness();
			}
		};
		
		if (multithreading) {
			sunChecker.start();
			rainChecker.start();
			fitnessChecker.start();
			try {
				sunChecker.join();
				rainChecker.join();
				fitnessChecker.join();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		} else {
			sunChecker.run();
			rainChecker.run();
			fitnessChecker.run();
		}
		
		// tick each tree's root nodes and wasted fitness
		for (GeneTree t : trees) {
			t.tick();
		}
		
		// each 1000 ticks, try to reproduce or kill each tree
		tickCount++;
		if (tickCount % 1000 == 0)
			reproduce();
		
		ticksThisSec++;
	}
	
	private void reproduce() {
		// update fitnesss graph
		if (maxFitness > maxAlltime)
			maxAlltime = maxFitness;
		if (minFitness < minAlltime)
			minAlltime = minFitness;
		GeneTrees.fitnessPanel.addPoint(GeneTrees.GRAPHDATA_FITNESS_MAX, numGens, maxFitness);
		GeneTrees.fitnessPanel.addPoint(GeneTrees.GRAPHDATA_FITNESS_AVG, numGens, avgFitness);
		GeneTrees.fitnessPanel.addPoint(GeneTrees.GRAPHDATA_FITNESS_MIN, numGens, minFitness);
		
		// update population graph
		if (trees.size() > maxTreesAlltime)
			maxTreesAlltime = trees.size();
		GeneTrees.populationPanel.addPoint(GeneTrees.GRAPHDATA_POPULATION, numGens, trees.size());
		
		// update tree stat graph
		double treeNodes = 0;
		double treeLeaves = 0;
		double treeRaincatchers = 0;
		double treeStructs = 0;
		double treeRoots = 0;
		double treeSeeddroppers = 0;
		for (GeneTree t : trees) {
			for (TreeNode n : t.getAllNodes()) {
				if (n.getType() == NodeType.Leaf)
					treeLeaves++;
				if (n.getType() == NodeType.Raincatcher)
					treeRaincatchers++;
				if (n.getType() == NodeType.Struct)
					treeStructs++;
				if (n.getType() == NodeType.Root)
					treeRoots++;
				if (n.getType() == NodeType.SeedDropper)
					treeSeeddroppers++;
			}
			treeNodes += t.getNumNodes();
		}
		GeneTrees.treeStatPanel.addPoint(GeneTrees.GRAPHDATA_NODES_ALL, numGens, treeNodes/trees.size());
		GeneTrees.treeStatPanel.addPoint(GeneTrees.GRAPHDATA_NODES_LEAF, numGens, treeLeaves/trees.size());
		GeneTrees.treeStatPanel.addPoint(GeneTrees.GRAPHDATA_NODES_RAINCATCHER, numGens, treeRaincatchers/trees.size());
		GeneTrees.treeStatPanel.addPoint(GeneTrees.GRAPHDATA_NODES_STRUCTURE, numGens, treeStructs/trees.size());
		GeneTrees.treeStatPanel.addPoint(GeneTrees.GRAPHDATA_NODES_ROOT, numGens, treeRoots/trees.size());
		GeneTrees.treeStatPanel.addPoint(GeneTrees.GRAPHDATA_NODES_SEEDDROPPER, numGens, treeSeeddroppers/trees.size());
		
		// update weather stat graph
		GeneTrees.particleStatPanel.addPoint(GeneTrees.GRAPHDATA_PARTICLES_SUNDROPS, numGens, sun.size());
		GeneTrees.particleStatPanel.addPoint(GeneTrees.GRAPHDATA_PARTICLES_RAINDROPS, numGens, rain.size());
		GeneTrees.particleStatPanel.addPoint(GeneTrees.GRAPHDATA_PARTICLES_SEEDS, numGens, seeds.size());
		
		// reset the current tick number, increment the generation number
		tickCount = 0;
		numGens++;

		// sort the array of trees in order to remove the ones
		//List<GeneTree> treesSorted = new ArrayList<GeneTree>(trees);
		//Collections.sort(treesSorted);
		
		// check all trees for which to remove
		HashSet<GeneTree> toRemove = new HashSet<GeneTree>();
		for (GeneTree t : trees) {
			if (t.getFitness() < 0) { // ( || t.getFitnessPercentage() < 0.4) {
				toRemove.add(t);
			}
		}
		trees.removeAll(toRemove);
		
		// check trees for which to reproduce
		HashSet<GeneTree> toAdd = new HashSet<GeneTree>();
		for (GeneTree t : trees) {
			for (int i = 0; i < (int)((t.getFitnessPercentage() - 0.4)/0.2); i++) {
				double newXPos = t.getRoot().getXPos() + (Math.random()-0.5)*200;
				GeneTree newTree = new GeneTree(t, (int)newXPos, (int)getGroundLevel(newXPos));
				if (newTree.getxMin() < simWidth && newTree.getxMax() > 0)
					toAdd.add(newTree);
			}
			t.resetFitness();
		}
		trees.addAll(toAdd);
		
		// if there are less than 100 trees left, repopulate
		while (trees.size() < 100) {
			double xPos = Math.random()*simWidth;
			trees.add(new GeneTree((int)(xPos), (int)getGroundLevel(xPos)));
		}
	}
	
	public double getGroundLevel(double x) {
		double sum = 0;
		for (int i = 0; i < groundDegree; i++) {
			sum += Math.cos(groundFreq[i]*x + groundDisp[i])*groundAmp[i];
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
	
	public int getSimWidth() {
		return simWidth;
	}

	public int getSimHeight() {
		return simHeight;
	}

	public long getMinFitness() {
		return minFitness;
	}

	public long getMaxFitness() {
		return maxFitness;
	}

	public long getAvgFitness() {
		return avgFitness;
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
	
	public long getTickCount() {
		return tickCount;
	}
	
	public int getNumGens() {
		return numGens;
	}
	
	public void setNumGens(int n) {
		numGens = n;
	}
}
