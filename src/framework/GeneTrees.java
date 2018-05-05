package framework;
import java.awt.Color;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;

import javax.swing.JFrame;
import javax.swing.SwingUtilities;

import grapher.GraphPanel;

public class GeneTrees implements Runnable {
	public static final String ver = "0.5.0";
	public static JFrame frame;
	public static GeneTreesPanel panel;
	public static GraphPanel fitnessPanel, populationPanel;
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
		fitnessPanel.addDataset(0, "Max Fitness", Color.BLUE);
		fitnessPanel.addDataset(1, "Average Fitness", Color.GREEN);
		fitnessPanel.addDataset(2, "Min Fitness", Color.RED);
		fitnessFrame.add(fitnessPanel);
		
		JFrame populationFrame = new JFrame("Population");
		populationFrame.setVisible(true);
		populationFrame.setSize(600, 600);
		populationPanel = new GraphPanel();
		populationPanel.addDataset(0, "Population", Color.BLACK);
		populationFrame.add(populationPanel);
		
		panel.init();
	}
}