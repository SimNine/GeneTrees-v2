package xyz.urffer.genetrees2.framework;

public class Parameters {

	// simulation
	public long simSeed = 10;
	public int simNumThreads = 15;
	public boolean simTakeSnapshots = true;

	// environment
	public int envWidth = 3000;
	public int envHeight = 2000;
	public int envGroundLevel = 1200;
	public int envNumWarmupTicks = 1000;
	public int envTicksPerGeneration = 1000;
	public int envSpontaneousTreesPerGen = 200;
	
	// particles
	public long particleRainBasePower = -50000;
	public long particleRainPowerChangePerTick = 55;
	public long particleSunBasePower = 70000;
	public long particleSunPowerChangePerTick = -45;
	
	// fitness
	public double fitnessRootNutrientCollectionPerSize = 3;
	public double fitnessRootNutrientCollectionPerDepth = 0.03;
	public long fitnessStructDecayPerSize = 1;
	public long fitnessActiveNodeDecayPerSize = 2;
	public long fitnessInactiveNodeDecayPerSize = 10;
	public long fitnessGainPerPowerAndNutrient = 2;
	public long fitnessRequirementPerChildPerNode = 5000;
	
	// tree constants
	public int constantNodeMinimumSize = 10;
	public double constantNodeMinimumDistance = 40.0;
	
	// mutation
	public double mutationTreeBaseChance = 0.3;
	public double mutationChanceNodeType = 0.15;
	public double mutationChanceNodeSize = 0.30;
	public double mutationChanceNodeAngle = 0.25;
	public double mutationChanceNodeDistance = 0.15;
	public double mutationChanceNodeAddition = 0.30;
	public double mutationChanceNodeDeletion = 0.10;

	public Parameters() {}
	
}
