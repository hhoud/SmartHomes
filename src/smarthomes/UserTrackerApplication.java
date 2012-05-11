/****************************************************************************
 *                                                                           *
 *  OpenNI 1.x Alpha                                                         *
 *  Copyright (C) 2011 PrimeSense Ltd.                                       *
 *                                                                           *
 *  This file is part of OpenNI.                                             *
 *                                                                           *
 *  OpenNI is free software: you can redistribute it and/or modify           *
 *  it under the terms of the GNU Lesser General Public License as published *
 *  by the Free Software Foundation, either version 3 of the License, or     *
 *  (at your option) any later version.                                      *
 *                                                                           *
 *  OpenNI is distributed in the hope that it will be useful,                *
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of           *
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the             *
 *  GNU Lesser General Public License for more details.                      *
 *                                                                           *
 *  You should have received a copy of the GNU Lesser General Public License *
 *  along with OpenNI. If not, see <http://www.gnu.org/licenses/>.           *
 *                                                                           *
 ****************************************************************************/
package smarthomes;

import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JFrame;

public class UserTrackerApplication {

    public static UserTracker viewer;
    private boolean shouldRun = true;
    private JFrame frame;
    private static boolean drawRGB = true;
    private static int lightValueCounter = 0;
    private static int darkValueCounter = 0;
    private static int lightHistCounter = 0;
    private static int darkHistCounter = 0;

    public UserTrackerApplication(JFrame frame) {
        this.frame = frame;
        frame.addKeyListener(new KeyListener() {

            @Override
            public void keyTyped(KeyEvent arg0) {
            }

            @Override
            public void keyReleased(KeyEvent arg0) {
            }

            @Override
            public void keyPressed(KeyEvent arg0) {
                if (arg0.getKeyCode() == KeyEvent.VK_ESCAPE) {
                    shouldRun = false;
                } else if (arg0.getKeyCode() == KeyEvent.VK_I) {
                    if (drawRGB) {
                        drawRGB = false;
                        viewer.setDrawRGB(drawRGB);
                    }
                } else if (arg0.getKeyCode() == KeyEvent.VK_R) {
                    if (!drawRGB) {
                        drawRGB = true;
                        viewer.setDrawRGB(drawRGB);
                    }
                }
            }
        });
    }

    public static void main(String s[]) {
        JFrame f = new JFrame("OpenNI User Tracker");
        f.addWindowListener(new WindowAdapter() {

            public void windowClosing(WindowEvent e) {
                System.exit(0);
            }
        });
        UserTrackerApplication app = new UserTrackerApplication(f);

        //Setup for communication
        //setupService();
        setupListeners();

        //Run the kinect
        app.viewer = new UserTracker();
        f.add("Center", app.viewer);
        f.pack();
        f.setVisible(true);
        app.run();
    }

    void run() {
        while (shouldRun) {
            viewer.updateDepth();
            if (!drawRGB) {
                viewer.updateIR();
            }
            viewer.repaint();
        }
        frame.dispose();
    }

    private static void setupService() {
        try {
            //setup webserver to listen on localhost:80
            Webserver server = new Webserver(80);

            //open broadcast socket on port 15000
            DatagramSocket broadcast_socket = new DatagramSocket();
            broadcast_socket.setBroadcast(true);

            //{optional} mac address hash
            String macHash = "12345678909876543212345678909876";
            //get IP address from this machine

            //Create broadcast message
            String broadcast_msg = "Sensor online, MACHash=" + macHash + ", IP=192.168.137.89";
            //Create broadcast packet
            DatagramPacket broadcast_packet = new DatagramPacket(broadcast_msg.getBytes(), broadcast_msg.getBytes().length, InetAddress.getByName("255.255.255.255"), 15000);
            //Send Broadcast packet
            broadcast_socket.send(broadcast_packet);

        } catch (IOException ex) {
            Logger.getLogger(UserTrackerApplication.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private static void setupListeners() {
        //Setup listeners for incoming data
        EnvironmentVars.getInstance().addPropertyChangeListener("lightValue", new LightValueChangeListener());
        EnvironmentVars.getInstance().addPropertyChangeListener("histIntensity", new HistIntensityChangeListener());
        EnvironmentVars.getInstance().addPropertyChangeListener("personPresent", new PersonPresentChangeListener());
    }

    static class LightValueChangeListener implements PropertyChangeListener {

        @Override
        public void propertyChange(PropertyChangeEvent e) {
            int threshold = 400;
            int lightValue = Integer.parseInt(e.getNewValue().toString());
            
            //check if the value is within the threshold long enough
            if(lightValue < threshold){
                lightValueCounter = 0;
                darkValueCounter++;
                if(darkValueCounter > 10){
                    viewer.setDrawRGB(false);
                    darkValueCounter = 0;
                }
            }else if(lightValue > threshold){
                darkValueCounter = 0;
                lightValueCounter++;
                if(lightValueCounter > 10){
                    viewer.setDrawRGB(true);
                    lightValueCounter = 0;
                }
            }
        }
    }

    static class HistIntensityChangeListener implements PropertyChangeListener {

        @Override
        public void propertyChange(PropertyChangeEvent e) {
            int threshold = 40;
            int histIntensity = Integer.parseInt(e.getNewValue().toString());
            
            //check if the value is within the threshold long enough
            if(histIntensity < threshold){
                lightHistCounter = 0;
                darkHistCounter++;
                if(darkHistCounter > 300){
                    viewer.setDrawRGB(false);
                    darkHistCounter = 0;
                }
            }else if(histIntensity > threshold){
                darkHistCounter = 0;
                lightHistCounter++;
                if(lightHistCounter > 300){
                    viewer.setDrawRGB(true);
                    lightHistCounter = 0;
                }
            }
        }
    }

    static class PersonPresentChangeListener implements PropertyChangeListener {

        @Override
        public void propertyChange(PropertyChangeEvent e) {
            //set the presence of a person to force the face detection
            viewer.setMustFind(Boolean.parseBoolean(e.getNewValue().toString()));
        }
    }
}