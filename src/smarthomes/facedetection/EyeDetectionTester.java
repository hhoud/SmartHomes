/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package smarthomes.facedetection;

import java.io.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import static com.googlecode.javacv.cpp.opencv_core.*;
import static com.googlecode.javacv.cpp.opencv_highgui.*;

/**
 *
 * @author hh354
 */
public class EyeDetectionTester {
    private static boolean drawRGB = true;
            
    public static void main(String[] args) {
        File dir = new File("faces/highres_rgb_testfiles/");
        String[] files = dir.list();
        for(int i=0;i<files.length;i++){
            File f = new File(dir,files[i]);
            if(!f.isDirectory()){
                IplImage img = cvLoadImage(f.getPath());
                detectEyes(img,f);
            }
        }
    }
    
    private static void detectEyes(IplImage img, File f){
        try{
            boolean hasFace = false;
            BufferedWriter out = new BufferedWriter(new FileWriter("eyes/results.txt", true));
            
            double[] scales = new double[]{1.05,1.1,1.15,1.20,1.25};
            int[] neighbours = new int[]{3,5,10,20};
            int[] sizes = new int[]{0,10,20,30};
            
            //Run a second face detection algorithm if the first one didn't find anything
            EyeDetection edetect = new EyeDetection();
            for(int i=0;i<scales.length;i++){
                for(int j=0;j<neighbours.length;j++){
                    for(int k=0;k<sizes.length;k++){
                        long start = System.nanoTime();
                        
                        java.util.Map<String,Object> result = null; //edetect.detectEyesTest(img, drawRGB, scales[i], neighbours[j], sizes[k]);
                        img = (IplImage)result.get("image");
                        hasFace = Boolean.parseBoolean(result.get("hasFaces").toString());
                        long elapsedTime = System.nanoTime() - start;
                        if(hasFace)
                            cvSaveImage("eyes/detected/"+f.getName()+"_"+scales[i]+"_"+neighbours[j]+"_"+sizes[k]+".jpg", img);
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
            Logger.getLogger(EyeDetectionTester.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}
