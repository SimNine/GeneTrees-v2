package xyz.urffer.genetrees2.framework;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;

import javax.swing.Timer;

import xyz.urffer.genetrees2.environment.EnvironmentParameters;
import xyz.urffer.genetrees2.environment.genetree.GeneTree;
import xyz.urffer.genetrees2.simulation.Simulation;

import xyz.urffer.urfutils.pannablepanel.PannablePanel;

@SuppressWarnings("serial")
public class GeneTreesPanel extends PannablePanel {
	
	private Simulation sim;
	
	private Timer repaintTimer = new Timer(1000/60, new ActionListener() {
		public void actionPerformed(ActionEvent evt) {
			if (isDrawing) {
				repaint();
			}
		}
	});
	private boolean isDrawing = true;
	
	public GeneTreesPanel(int width, int height) {
		super(width, height, true);
		
		sim = new Simulation(EnvironmentParameters.ENVIRONMENT_SEED, true);
		
		addKeyListener(new KeyListener() {
			public void keyPressed(KeyEvent e) {
				switch (e.getKeyCode()) {
				case KeyEvent.VK_P:
					sim.setRunning(!sim.isRunning());
					break;
				case KeyEvent.VK_D:
					GeneTrees.debug = !GeneTrees.debug;
					break;
				case KeyEvent.VK_F1:
					Loader.saveGame();
					break;
				case KeyEvent.VK_F2:
					Loader.loadGame();
					break;
				case KeyEvent.VK_Q:
					isDrawing = !isDrawing;
					break;
				case KeyEvent.VK_M:
					sim.setMultithreading(!sim.isMultithreading());
					break;
				case KeyEvent.VK_T:
					sim.tick();
					break;
				}
			}
			public void keyReleased(KeyEvent e) {}
			public void keyTyped(KeyEvent e) {}
		});
		
		addMouseListener(new MouseListener() {
			public void mouseClicked(MouseEvent e) {
				sim.setTrackedTreeAt(xMouse + xScr, yMouse + yScr);
			}
			public void mouseEntered(MouseEvent arg0) {}
			public void mouseExited(MouseEvent arg0) {}
			public void mousePressed(MouseEvent arg0) {}
			public void mouseReleased(MouseEvent arg0) {}
		});
		
		addMouseMotionListener(new MouseMotionListener() {
			public void mouseDragged(MouseEvent e) {
				repaint();
			}
			public void mouseMoved(MouseEvent e) {}
		});
		
		this.repaintTimer.start();
	}

	public void paintComponent(Graphics g) {
		// draw the simulation
		sim.draw(g, this.xScr, this.yScr, this.getWidth(), this.getHeight());
		
		// draw the selected tree display box
		drawTrackedTree(g);
		
		// draw the debug text
		int fh = 15; // fontHeight
		int ln = 1; // lineNum
		GeneTree trackedTree = sim.getTrackedTree();
		
		g.setColor(Color.BLACK);
		g.drawString("Tick speed: " + sim.getTicksLastSec() + "/s", 0, fh*ln++);
		g.drawString("Tick number: " + sim.getTickCount(), 0, fh*ln++);
		g.drawString("Generation number: " + sim.getNumGens(), 0, fh*ln++);
		ln++;
		g.drawString("Screen Top-left: " + xScr + "," + yScr, 0, fh*ln++);
		ln++;
		if (trackedTree == null) {
			g.drawString("Click on a tree to track it.", 0, fh*ln++);
		} else {
			g.drawString("Current tree's energy: " + trackedTree.getEnergy(), 0, fh*ln++);
			g.drawString("Current tree's nutrients: " + trackedTree.getNutrients(), 0, fh*ln++);
			g.drawString("Current tree's fitness: " + trackedTree.getFitness(), 0, fh*ln++);
			g.drawString("Current tree's fitness percentile: " + trackedTree.getFitnessPercentage(), 0, fh*ln++);
		}
		ln++;
		g.drawString("Minimum fitness: " + sim.getMinFitness(), 0, fh*ln++);
		g.drawString("Maximum fitness: " + sim.getMaxFitness(), 0, fh*ln++);
		g.drawString("Average fitness: " + sim.getAvgFitness(), 0, fh*ln++);
		g.drawString("Number of trees: " + sim.getEnv().getTrees().size(), 0, fh*ln++);
		ln++;
		g.drawString("Controls:", 0, fh*ln++);
		g.drawString("Pan screen: drag mouse", 0, fh*ln++);
		g.drawString("Pause simulation: P", 0, fh*ln++);
		g.drawString("Pause drawing (but continue simulation): Q", 0, fh*ln++);
		g.drawString("Toggle debug features: D", 0, fh*ln++);
		g.drawString("Toggle multithreading: M", 0, fh*ln++);
		g.drawString("Single manual tick: T", 0, fh*ln++);
		ln++;
		g.drawString("Is running: " + sim.isRunning(), 0, fh*ln++);
		g.drawString("Is drawing: " + isDrawing, 0, fh*ln++);
		g.drawString("Is debug: " + GeneTrees.debug, 0, fh*ln++);
		g.drawString("Is multithreading: " + sim.isMultithreading(), 0, fh*ln++);
	}
	
	private void drawTrackedTree(Graphics g) {
		GeneTree trackedTree = sim.getTrackedTree();
		
		if (trackedTree == null)
			return;
		
		int w = trackedTree.getxMax()-trackedTree.getxMin();
		int h = trackedTree.getyMax()-trackedTree.getyMin();
		
		g.setColor(Color.WHITE);
		g.fillRect(getWidth() - w - 30, getHeight() - h - 30, w + 20, h + 20);
		trackedTree.draw(g, 
						 trackedTree.getxMin() - (getWidth() - w - 20), 
						 trackedTree.getyMin() - (getHeight() - h - 20));
		
		g.setColor(Color.BLACK);
		g.drawString("Selected Tree:", getWidth() - w - 30, getHeight() - h - 15);
	}
	
	public Simulation getSimulation() {
		return sim;
	}
}