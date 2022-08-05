package xyz.urffer.genetrees2.framework;

import java.awt.Color;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;

import javax.swing.JFrame;
import javax.swing.JTabbedPane;
import javax.swing.SwingUtilities;

import xyz.urffer.urfutils.grapher.GraphPanel;

public class GeneTrees implements Runnable {
	public static final String ver = "0.6.0";
	public static JFrame frame;
	public static GeneTreesPanel panel;
	
	public static GraphPanel fitnessPanel, populationPanel, nodeStatPanel, particleStatPanel, performancePanel;
	
	public static final String GRAPHDATA_FITNESS_MAX = "max";
	public static final String GRAPHDATA_FITNESS_AVG = "avg";
	public static final String GRAPHDATA_FITNESS_MIN = "min";
	public static final String GRAPHDATA_FITNESS_AVG_ENERGY = "avg_energy";
	public static final String GRAPHDATA_FITNESS_AVG_NUTRIENTS = "avg_nutrients";
	
	public static final String GRAPHDATA_POPULATION = "population";
	
	public static final String GRAPHDATA_NODES_ALL = "all";
	public static final String GRAPHDATA_NODES_LEAF = "leaf";
	public static final String GRAPHDATA_NODES_RAINCATCHER = "raincatcher";
	public static final String GRAPHDATA_NODES_ROOT = "root";
	public static final String GRAPHDATA_NODES_STRUCTURE = "structure";
	public static final String GRAPHDATA_NODES_SEEDDROPPER = "seeddropper";
	
	public static final String GRAPHDATA_PARTICLES_SUNDROPS = "sundrops";
	public static final String GRAPHDATA_PARTICLES_RAINDROPS = "raindrops";
	public static final String GRAPHDATA_PARTICLES_SEEDS = "seeds";

	public static final String GRAPHDATA_PERFORMANCE_MILLISECONDS_PER_GENERATION = "mpg";
	
	public static boolean debug = false;

	public static void main(String[] args) {
		SwingUtilities.invokeLater(new GeneTrees());
	}

	@Override
	public void run() {
		frame = new JFrame("GeneTrees2 " + ver);
		
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setSize(800, 600);
		frame.setUndecorated(false);
		frame.setVisible(true);
		
		panel = new GeneTreesPanel(800, 600);
		frame.add(panel);
		
		frame.addComponentListener(new ComponentAdapter() {
            public void componentResized(ComponentEvent e) {
                panel.setSize(frame.getContentPane().getWidth(), frame.getContentPane().getHeight());
            }
        });
		
		JFrame graphFrame = new JFrame("Tree Data");
		graphFrame.setVisible(true);
		graphFrame.setSize(600, 600);
		
		JTabbedPane graphTabs = new JTabbedPane();
		graphFrame.add(graphTabs);
		
		fitnessPanel = new GraphPanel();
		fitnessPanel.addDataset(GeneTrees.GRAPHDATA_FITNESS_MAX, "Max Fitness", Color.BLUE);
		fitnessPanel.addDataset(GeneTrees.GRAPHDATA_FITNESS_AVG, "Average Fitness", Color.GREEN);
		fitnessPanel.addDataset(GeneTrees.GRAPHDATA_FITNESS_MIN, "Min Fitness", Color.RED);
		fitnessPanel.addDataset(GeneTrees.GRAPHDATA_FITNESS_AVG_NUTRIENTS, "Average Nutrients", new Color(128, 128, 0));
		fitnessPanel.addDataset(GeneTrees.GRAPHDATA_FITNESS_AVG_ENERGY, "Average Energy", Color.ORANGE);
		graphTabs.addTab("Fitness", null, fitnessPanel, "Average fitness of all trees");

		populationPanel = new GraphPanel();
		populationPanel.addDataset(GeneTrees.GRAPHDATA_POPULATION, "Population", Color.BLACK);
		graphTabs.addTab("Population", null, populationPanel, "Population of all trees");

		nodeStatPanel = new GraphPanel();
		nodeStatPanel.addDataset(GeneTrees.GRAPHDATA_NODES_ALL, "Avg. Number of Nodes", Color.GRAY);
		nodeStatPanel.addDataset(GeneTrees.GRAPHDATA_NODES_LEAF, "Avg. Number of Leaf Nodes", Color.GREEN);
		nodeStatPanel.addDataset(GeneTrees.GRAPHDATA_NODES_RAINCATCHER, "Avg. Number of Raincatcher Nodes", Color.BLUE);
		nodeStatPanel.addDataset(GeneTrees.GRAPHDATA_NODES_STRUCTURE, "Avg. Number of Structure Nodes", Color.BLACK);
		nodeStatPanel.addDataset(GeneTrees.GRAPHDATA_NODES_ROOT, "Avg. Number of Root Nodes", new Color(128, 128, 0));
		nodeStatPanel.addDataset(GeneTrees.GRAPHDATA_NODES_SEEDDROPPER, "Avg. Number of Seeddropper Nodes", Color.GREEN.darker());
		graphTabs.addTab("Node Stats", null, nodeStatPanel, "Avg number of each type of node");

		particleStatPanel = new GraphPanel();
		particleStatPanel.addDataset(GeneTrees.GRAPHDATA_PARTICLES_SUNDROPS, "Number of Sundrops", Color.YELLOW);
		particleStatPanel.addDataset(GeneTrees.GRAPHDATA_PARTICLES_RAINDROPS, "Number of Raindrops", Color.BLUE);
		particleStatPanel.addDataset(GeneTrees.GRAPHDATA_PARTICLES_SEEDS, "Number of Seeds", Color.GREEN.darker());
		graphTabs.addTab("Particles", null, particleStatPanel, "Avg number of each type of particle");
		
		performancePanel = new GraphPanel();
		performancePanel.addDataset(GeneTrees.GRAPHDATA_PERFORMANCE_MILLISECONDS_PER_GENERATION, "Milliseconds per generation", Color.BLACK);
		graphTabs.addTab("Performance", null, performancePanel, "Simulation performance statistics");
	}
}
