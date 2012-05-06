/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package smarthomes.facedetection;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import jjil.algorithm.Gray8Rgb;
import jjil.algorithm.RgbAvgGray;
import jjil.core.Error;
import jjil.core.Image;
import jjil.core.RgbImage;
import jjil.j2se.RgbImageJ2se;
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
        File dir = new File("faces/highres_rgb_testfiles/");
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
            BufferedWriter out = new BufferedWriter(new FileWriter("faces/results.txt", true));
            
            double[] scales = new double[]{1.05,1.1,1.15,1.2,1.25,1.3,1.35,1.4};
            int[] neighbours = new int[]{1,2,3};
            int[] sizes = new int[]{40};
            
            //Run a face detection algorithm if the first one didn't find anything
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
                
        }catch (Exception ex) {
            Logger.getLogger(FaceDetectionTester.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    private static void detectFaces2(IplImage img, File f){
        try{
            boolean hasFace = false;
            BufferedWriter out = new BufferedWriter(new FileWriter("results.txt", true));
            
            long start = System.nanoTime();

            //Run a first face detection algorithm
            final DetectFace detect = new DetectFace(img.getBufferedImage());
            hasFace = detect.detectFaces();
            img = IplImage.createFrom(detect.getImg());
            long elapsedTime = System.nanoTime() - start;
            
            if(hasFace)
                cvSaveImage("faces/detected/"+f.getName()+".jpg", img);
            
            String success = (hasFace)?"1":"0";
            String fname = f.getName().split("\\.")[0];
            String text = fname+";"+elapsedTime+";"+success;
            out.write(text);
            out.newLine();
            out.close();
        }catch (Exception ex) {
            Logger.getLogger(FaceDetectionTester.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    private static void detectFaces3(IplImage img, File f){
        try{
            boolean hasFace = false;
            BufferedWriter out = new BufferedWriter(new FileWriter("results.txt", true));
            
            long start = System.nanoTime();

            //Run a first face detection algorithm
            int minScale = 1;
            int maxScale = 40;
            // step #2 - convert BufferedImage to JJIL Image
            RgbImage im = RgbImageJ2se.toRgbImage(img.getBufferedImage());
            // step #3 - convert image to greyscale 8-bits
            RgbAvgGray toGray = new RgbAvgGray();
            toGray.push(im);
            // step #4 - initialise face detector with correct Haar profile
            InputStream is  = FaceDetectionTester.class.getResourceAsStream("/jjilexample/haar/profileface.txt");
            Gray8DetectHaarMultiScale detectHaar = new Gray8DetectHaarMultiScale(is, minScale, maxScale);
            // step #5 - apply face detector to grayscale image
            List results = detectHaar.pushAndReturn(toGray.getFront());
            // step #6 - retrieve resulting face detection mask
            Image i = detectHaar.getFront();
            // finally convert back to RGB image to write out to .jpg file
            Gray8Rgb g2rgb = new Gray8Rgb();
            g2rgb.push(i);
            long elapsedTime = System.nanoTime() - start;
            RgbImageJ2se conv = new RgbImageJ2se();
            conv.toFile((RgbImage)g2rgb.getFront(), "faces/detected/"+f.getName()+".jpg");
            
            String success = (results.size()>0)?"1":"0";
            String fname = f.getName().split("\\.")[0];
            String text = fname+";"+elapsedTime+";"+success;
            out.write(text);
            out.newLine();
            out.close();
        } catch (Error ex) {
            Logger.getLogger(FaceDetectionTester.class.getName()).log(Level.SEVERE, null, ex);
        }catch (Exception ex) {
            Logger.getLogger(FaceDetectionTester.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}
