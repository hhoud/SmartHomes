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

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JFrame;

public class UserTrackerApplication {

    public UserTracker viewer;
    private boolean shouldRun = true;
    private JFrame frame;
    private boolean drawRGB = true;

    public UserTrackerApplication (JFrame frame)
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
        JFrame f = new JFrame("OpenNI User Tracker");
        f.addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {System.exit(0);}
        });
        UserTrackerApplication app = new UserTrackerApplication(f);

        //Setup for communication
        setupService();
        
        /* app.viewer = new UserTracker();
        f.add("Center", app.viewer);
        f.pack();
        f.setVisible(true);
        app.run(); */
    }

    void run()
    {
        while(shouldRun) {
            viewer.updateDepth();
            if(!drawRGB)
                viewer.updateIR();
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
            String broadcast_msg = "Sensor online, MACHash=" + macHash + ", IP=192.168.137.161";
            //Create broadcast packet
            DatagramPacket broadcast_packet = new DatagramPacket(broadcast_msg.getBytes(), broadcast_msg.getBytes().length, InetAddress.getByName("255.255.255.255"),15000);
            //Send Broadcast packet
            broadcast_socket.send(broadcast_packet);
            
        } catch (IOException ex) {
            Logger.getLogger(UserTrackerApplication.class.getName()).log(Level.SEVERE, null, ex);
        }        
    }
}