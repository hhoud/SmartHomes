/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package smarthomes;

import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import javax.swing.JFrame;

/**
 *
 * @author hh354
 */
public class SmartHomes{
    
    public SmartHomeTracker viewer;
    private boolean shouldRun = true;
    private JFrame frame;
    private boolean drawRGB = false;

    public SmartHomes (JFrame frame)
    {
    	this.frame = frame;
    	frame.addKeyListener(new KeyListener()
		{
			@Override
			public void keyTyped(KeyEvent arg0) {}
			@Override
			public void keyReleased(KeyEvent arg0) {}
			@Override
			public void keyPressed(KeyEvent arg0) {
				if (arg0.getKeyCode() == KeyEvent.VK_ESCAPE)
                                    shouldRun = false;
				else if(arg0.getKeyCode() == KeyEvent.VK_I){
                                    if(drawRGB){
                                        drawRGB = false;
                                        viewer.setDrawRGB(drawRGB);
                                    }
                                }
                                else if(arg0.getKeyCode() == KeyEvent.VK_R){
                                    if(!drawRGB){
                                        drawRGB = true;
                                        viewer.setDrawRGB(drawRGB);
                                    }
                                }
			}
		});
    }

    public static void main(String s[])
    {
        JFrame f = new JFrame("Smart Home Tracker");
        f.addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {System.exit(0);}
        });
        SmartHomes app = new SmartHomes(f);
        
        app.viewer = new SmartHomeTracker();
        f.add("Center", app.viewer);
        f.pack();
        f.setVisible(true);
        app.run();
    }

    void run()
    {
        while(shouldRun) {
            viewer.updateDepth();
            /*if(!drawRGB)
                viewer.updateIR();*/
            viewer.repaint();
        }
        frame.dispose();
    }
}
