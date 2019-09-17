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
	
	private HashSet<GeneTree> treesToAdd = new HashSet<GeneTree>();
	private HashSet<GeneTree> treesToRemove = new HashSet<GeneTree>();
	
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
	private int bigTicks = 0;
	
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
	
	// computes the collision of add seeds with the ground and with trees
	public void computeSeeds() {
		// add new seeds
		// TODO
		
		// check all seeds
		HashSet<GeneSeed> remSeeds = new HashSet<GeneSeed>();
		if (remSeeds.size() > 0) {
			System.out.println("there are " + remSeeds.size() + " seeds");
		}
		for (GeneSeed s : seeds) {
			// tick each seed
			s.tick();
			
			// check if seed has hit ground
			if (s.getYPos() > getGroundLevel(s.getXPos())) {
				remSeeds.add(s);
				
				GeneTree newTree = new GeneTree(s.getTree(), s.getXPos(), (int)getGroundLevel(s.getXPos()));
				treesToAdd.add(newTree);
			}
		}
		
		// remove all seeds that have hit the ground
		seeds.removeAll(remSeeds);
	}
	
	// computes the collision of all sunspecks with the ground and with trees
	public void computeSun() {
		// add new sunspecks
		for (int i = 0; i < 2; i++) {
			double pct = Math.random();
			pct = pct * pct;
			sun.add(new SunSpeck((int)(pct * (double)simWidth), 0));
		}
		
		// compute all sunspecks
		HashSet<SunSpeck> remSun = new HashSet<SunSpeck>();
		for (SunSpeck s : sun) {
			// tick each sunspeck
			s.tick();
			
			// check if sunspeck has hit ground
			if (s.getYPos() > getGroundLevel(s.getXPos())) {
				remSun.add(s);
			}
			
			// check collision of sunspecks with each tree
			for (GeneTree t : trees) {
				for (TreeNode n : t.getAllNodes()) {
					int sx = s.getXPos();
					int sy = s.getYPos();
					int nx = n.getXPos();
					int ny = n.getYPos();
					int nd = n.getSize() / 2;

					// if the sunspeck hits this node, remove it
					if ((nx - sx) * (nx - sx) + (ny - sy) * (ny - sy) < nd * nd) {
						remSun.add(s);

						// if this node is a leaf, increment its fitness
						if (n.getType() == NodeType.Leaf) {
							n.setActivated(true);
							t.setEnergy(t.getEnergy() + s.getPower());
						}
					}
				}
			}
		}
		
		// remove all specks that have hit the ground
		sun.removeAll(remSun);
	}
	
	// check collision of raindrops with ground and with trees
	public void computeRain() {
		// add new raindrops
		for (int i = 0; i < 2; i++) {
			double pct = Math.random();
			pct = 1.0 - (pct * pct);
			rain.add(new RainDrop((int)(pct * (double)simWidth), 0));
		}
		
		// compute all raindrops
		HashSet<RainDrop> remRain = new HashSet<RainDrop>();
		for (RainDrop d : rain) {
			// tick each raindrop
			d.tick();
			
			// check if raindrop has hit ground
			if (d.getYPos() > getGroundLevel(d.getXPos())) {
				remRain.add(d);
			}
			
			// check for collision of raindrop with each tree
			for (GeneTree t : trees) {
				for (TreeNode n : t.getAllNodes()) {
					if (n.getType() != NodeType.Raincatcher) {
						continue;
					}
					
					int rx = d.getXPos();
					int ry = d.getYPos();
					int nx = n.getXPos();
					int ny = n.getYPos();
					int nd = n.getSize() / 2;

					// if the raindrop hits this node, remove it
					if ((nx - rx) * (nx - rx) + (ny - ry) * (ny - ry) < nd * nd) {
						n.setActivated(true);
						remRain.add(d);
						t.setEnergy(t.getEnergy() + d.getPower());
					}
				}
			}
		}

		// remove all raindrops that have collided with something
		rain.removeAll(remRain);
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
			if (GeneTrees.debug) {
				System.out.println("ticks per sec: " + ticksThisSec + " - multithreading: " + multithreading);
			}
			ticksThisSec = 0;
			prevTime = currTime;
		}
		
		Thread seedChecker = new Thread() {
			public void run() {
				GeneTrees.panel.getEnv().computeSeeds();
			}
		};
		
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
			seedChecker.start();
			sunChecker.start();
			rainChecker.start();
			fitnessChecker.start();
			try {
				seedChecker.join();
				sunChecker.join();
				rainChecker.join();
				fitnessChecker.join();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		} else {
			seedChecker.run();
			sunChecker.run();
			rainChecker.run();
			fitnessChecker.run();
		}
		
		// tick each tree's root nodes and wasted fitness
		for (GeneTree t : trees) {
			t.tick();
			
			// if a genetree has significantly negative fitness, kill it
			if (t.getFitness() < t.getNumNodes()*-100000)
				treesToRemove.add(t);
			
			// if a genetree has significantly positive fitness, mutate it in place
			if (t.getFitness() > t.getNumNodes()*20000) {
				treesToRemove.add(t);
				treesToAdd.add(new GeneTree(t, t.getRoot().getXPos(), t.getRoot().getYPos()));
			}
		}
		
		// add and remove all waiting trees
		trees.addAll(treesToAdd);
		trees.removeAll(treesToRemove);
		
		// if there are less than 100 trees left, repopulate
		while (trees.size() < 100) {
			double xPos = Math.random()*simWidth;
			trees.add(new GeneTree((int)(xPos), (int)getGroundLevel(xPos)));
		}
		
		// each 1000 ticks, update the graphs
		tickCount++;
		if (tickCount % 1000 == 0) {
			tickCount = 0;
			updateGraphs();
		}
		
		ticksThisSec++;
	}
	
	private void updateGraphs() {
		// update fitnesss graph
		if (maxFitness > maxAlltime)
			maxAlltime = maxFitness;
		if (minFitness < minAlltime)
			minAlltime = minFitness;
		GeneTrees.fitnessPanel.addPoint(GeneTrees.GRAPHDATA_FITNESS_MAX, bigTicks, maxFitness);
		GeneTrees.fitnessPanel.addPoint(GeneTrees.GRAPHDATA_FITNESS_AVG, bigTicks, avgFitness);
		GeneTrees.fitnessPanel.addPoint(GeneTrees.GRAPHDATA_FITNESS_MIN, bigTicks, minFitness);
		
		// update population graph
		if (trees.size() > maxTreesAlltime)
			maxTreesAlltime = trees.size();
		GeneTrees.populationPanel.addPoint(GeneTrees.GRAPHDATA_POPULATION, bigTicks, trees.size());
		
		// update tree stat graph
		double treeNodes = 0;
		double treeLeaves = 0;
		double treeRaincatchers = 0;
		double treeStructs = 0;
		double treeRoots = 0;
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
			}
			treeNodes += t.getNumNodes();
		}
		GeneTrees.treeStatPanel.addPoint(GeneTrees.GRAPHDATA_NODES_ALL, bigTicks, treeNodes/trees.size());
		GeneTrees.treeStatPanel.addPoint(GeneTrees.GRAPHDATA_NODES_LEAF, bigTicks, treeLeaves/trees.size());
		GeneTrees.treeStatPanel.addPoint(GeneTrees.GRAPHDATA_NODES_RAINCATCHER, bigTicks, treeRaincatchers/trees.size());
		GeneTrees.treeStatPanel.addPoint(GeneTrees.GRAPHDATA_NODES_STRUCTURE, bigTicks, treeStructs/trees.size());
		GeneTrees.treeStatPanel.addPoint(GeneTrees.GRAPHDATA_NODES_ROOT, bigTicks, treeRoots/trees.size());
		
		// update weather stat graph
		GeneTrees.particleStatPanel.addPoint(GeneTrees.GRAPHDATA_PARTICLES_SUNDROPS, bigTicks, sun.size());
		GeneTrees.particleStatPanel.addPoint(GeneTrees.GRAPHDATA_PARTICLES_RAINDROPS, bigTicks, rain.size());
		GeneTrees.particleStatPanel.addPoint(GeneTrees.GRAPHDATA_PARTICLES_SEEDS, bigTicks, seeds.size());
		
		// increment the generation number
		bigTicks++;
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
		return bigTicks;
	}
	
	public void setNumGens(int n) {
		bigTicks = n;
	}
}
