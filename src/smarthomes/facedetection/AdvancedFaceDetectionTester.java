/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package smarthomes.facedetection;

import java.io.BufferedWriter;
import static com.googlecode.javacv.cpp.opencv_core.*;
import static com.googlecode.javacv.cpp.opencv_highgui.*;
import static com.googlecode.javacv.cpp.opencv_imgproc.*;
import java.io.File;
import java.io.FileWriter;
import java.util.logging.Level;
import java.util.logging.Logger;
import smarthomes.GammaCorrection;

/**
 *
 * @author root
 */
public class AdvancedFaceDetectionTester {
    private static boolean drawRGB = true;
            
    public static void main(String[] args) {
        File dir = new File("faces/ir_advanced_results/ir_without_glasses/frontal/");
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
            BufferedWriter out = new BufferedWriter(new FileWriter("faces/ir_without_glasses_frontal_results.txt", true));
            
            //Run a face detection algorith
            AdvancedFaceDetection fdetect = new AdvancedFaceDetection();
            long start = System.nanoTime();

            //Perform automatic gamma correction
            GammaCorrection gc = new GammaCorrection();
            img = gc.correctGamma(img, drawRGB);
            cvSmooth(img, img, CV_MEDIAN, 5);
        
            java.util.Map<String,Object> result = fdetect.detectFaces(img, drawRGB);
            img = (IplImage)result.get("image");
            hasFace = Boolean.parseBoolean(result.get("hasFaces").toString());
            long elapsedTime = System.nanoTime() - start;
            if(hasFace)
                cvSaveImage("faces/detected/"+f.getName(), img);
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
}
