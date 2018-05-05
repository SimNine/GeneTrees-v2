package framework;
import java.awt.Color;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;

import javax.swing.JFrame;
import javax.swing.SwingUtilities;

import urf.grapher.*;

public class GeneTrees implements Runnable {
	public static final String ver = "0.6.0";
	public static JFrame frame;
	public static GeneTreesPanel panel;
	
	public static GraphPanel fitnessPanel, populationPanel, treeStatPanel, particleStatPanel;
	public static final String GRAPHDATA_FITNESS_MAX = "max";
	public static final String GRAPHDATA_FITNESS_AVG = "avg";
	public static final String GRAPHDATA_FITNESS_MIN = "min";
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
	
	public static boolean debug = true;

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
		
		JFrame fitnessFrame = new JFrame("Fitness");
		fitnessFrame.setVisible(true);
		fitnessFrame.setSize(600, 600);
		fitnessPanel = new GraphPanel();
		fitnessPanel.addDataset(GeneTrees.GRAPHDATA_FITNESS_MAX, "Max Fitness", Color.BLUE);
		fitnessPanel.addDataset(GeneTrees.GRAPHDATA_FITNESS_AVG, "Average Fitness", Color.GREEN);
		fitnessPanel.addDataset(GeneTrees.GRAPHDATA_FITNESS_MIN, "Min Fitness", Color.RED);
		fitnessFrame.add(fitnessPanel);
		
		JFrame populationFrame = new JFrame("Population");
		populationFrame.setVisible(true);
		populationFrame.setSize(600, 600);
		populationPanel = new GraphPanel();
		populationPanel.addDataset(GeneTrees.GRAPHDATA_POPULATION, "Population", Color.BLACK);
		populationFrame.add(populationPanel);
		
		JFrame treeStatFrame = new JFrame("Tree Stats");
		treeStatFrame.setVisible(true);
		treeStatFrame.setSize(600, 600);
		treeStatPanel = new GraphPanel();
		treeStatPanel.addDataset(GeneTrees.GRAPHDATA_NODES_ALL, "Avg. Number of Nodes", Color.GRAY);
		treeStatPanel.addDataset(GeneTrees.GRAPHDATA_NODES_LEAF, "Avg. Number of Leaf Nodes", Color.GREEN);
		treeStatPanel.addDataset(GeneTrees.GRAPHDATA_NODES_RAINCATCHER, "Avg. Number of Raincatcher Nodes", Color.BLUE);
		treeStatPanel.addDataset(GeneTrees.GRAPHDATA_NODES_STRUCTURE, "Avg. Number of Structure Nodes", Color.BLACK);
		treeStatPanel.addDataset(GeneTrees.GRAPHDATA_NODES_ROOT, "Avg. Number of Root Nodes", new Color(128, 128, 0));
		treeStatPanel.addDataset(GeneTrees.GRAPHDATA_NODES_SEEDDROPPER, "Avg. Number of Seeddropper Nodes", Color.GREEN.darker());
		treeStatFrame.add(treeStatPanel);
		
		JFrame particleStatFrame = new JFrame("Weather Stats");
		particleStatFrame.setVisible(true);
		particleStatFrame.setSize(600, 600);
		particleStatPanel = new GraphPanel();
		particleStatPanel.addDataset(GeneTrees.GRAPHDATA_PARTICLES_SUNDROPS, "Number of Sundrops", Color.YELLOW);
		particleStatPanel.addDataset(GeneTrees.GRAPHDATA_PARTICLES_RAINDROPS, "Number of Raindrops", Color.BLUE);
		particleStatPanel.addDataset(GeneTrees.GRAPHDATA_PARTICLES_SEEDS, "Number of Seeds", Color.GREEN.darker());
		particleStatFrame.add(particleStatPanel);
		
		panel.startTime();
	}
}