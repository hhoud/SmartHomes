/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package smarthomes.facedetection;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.logging.Level;
import java.util.logging.Logger;
import static com.googlecode.javacv.cpp.opencv_core.*;
import static com.googlecode.javacv.cpp.opencv_highgui.*;
        
/**
 *
 * @author hh354
 */
public class FaceDetectionTester {
    
    private static boolean drawRGB = true;
    private static Thread advFaceDetectionThread;
            
    public static void main(String[] args) {
        File dir = new File("faces/testfiles/");
        String[] files = dir.list();
        for(int i=0;i<files.length;i++){
            File f = new File(dir,files[i]);
            if(!f.isDirectory()){
                IplImage img = cvLoadImage(f.getPath());
                detectFaces(img,f);
            }
        }
    }
    
    private static void detectFaces(IplImage img, File f){
        try{
            boolean hasFace = false;
            BufferedWriter out = new BufferedWriter(new FileWriter("results.txt", true));
            
            /*//Run a first face detection algorithm
            final DetectFace detect = new DetectFace(img.getBufferedImage());
            hasFace = detect.detectFaces();
            img = IplImage.createFrom(detect.getImg());*/
            
            double[] scales = new double[]{1.05,1.10,1.15,1.20,1.25,1.30,1.35,1.40};
            int[] neighbours = new int[]{1,2,3};
            int[] sizes = new int[]{20};
            
            //Run a second face detection algorithm if the first one didn't find anything
            //if(!hasFace){
            FaceDetection fdetect = new FaceDetection();
            for(int i=0;i<scales.length;i++){
                for(int j=0;j<neighbours.length;j++){
                    for(int k=0;k<sizes.length;k++){
                        long start = System.nanoTime();
                        
                        java.util.Map<String,Object> result = fdetect.detectFacesTest(img, drawRGB, scales[i], neighbours[j], sizes[k]);
                        img = (IplImage)result.get("image");
                        hasFace = Boolean.parseBoolean(result.get("hasFaces").toString());
                        long elapsedTime = System.nanoTime() - start;
                        if(hasFace)
                            cvSaveImage("faces/detected/"+f.getName()+"_"+scales[i]+"_"+neighbours[j]+"_"+sizes[k]+".jpg", img);
                        String success = (hasFace)?"1":"0";
                        String fname = f.getName().split("\\.")[0];
                        String text = fname+";"+scales[i]+";"+neighbours[j]+";"+sizes[k]+";"+elapsedTime+";"+success;
                        out.write(text);
                        out.newLine();
                    }
                }
            }
            out.close();
                
            //}
            
            /*int user = Integer.parseInt(f.getName().split("_")[2]);
            //Save ROI
            //if(hasFace){
                cvSaveImage("faces/detected/"+f.getName(), img);
            }else{
                if(advFaceDetectionThread == null || !advFaceDetectionThread.isAlive()){
                    advFaceDetectionThread = new Thread(new AdvancedFaceDetection(img,drawRGB,user));
                    advFaceDetectionThread.start();
                }
            }*/
        }catch (Exception ex) {
            Logger.getLogger(FaceDetectionTester.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}
