package framework;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;

import javax.swing.JFrame;
import javax.swing.SwingUtilities;

//import urf.GraphWindow;

public class GeneTrees implements Runnable {
	public static final String ver = "0.2.1";
	public static JFrame frame;
	public static GeneTreesPanel panel;
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
		
		/*
		JFrame graphFrame = new JFrame("Graph");
		GraphWindow graphPanel = new GraphWindow();
		graphFrame.setVisible(true);
		graphFrame.setSize(600, 600);
		graphFrame.add(graphPanel);
		*/
		
		panel.init();
	}
}