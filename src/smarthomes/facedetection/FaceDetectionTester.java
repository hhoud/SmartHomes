/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package smarthomes.facedetection;

import com.googlecode.javacv.CanvasFrame;
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
import static com.googlecode.javacv.cpp.opencv_imgproc.*;
        
/**
 *
 * @author hh354
 */
public class FaceDetectionTester {
    
    private static boolean drawRGB = true;
    private static Thread advFaceDetectionThread;
            
    public static void main(String[] args) {
        File dir = new File("faces/testir2/");
        String[] files = dir.list();
        for(int i=0;i<files.length;i++){
            File f = new File(dir,files[i]);
            if(!f.isDirectory()){
                IplImage img = cvLoadImage(f.getPath());
                detectFaces(img,f);
                //imageAnalysis(img);
            }
        }
    }
    
    private static void imageAnalysis(IplImage iplBgrImage){
        //calculate average intensity
        //convert to hsi
        IplImage hsv_image = IplImage.create(cvSize(iplBgrImage.width(), iplBgrImage.height()), IPL_DEPTH_8U, 3);
        cvCvtColor(iplBgrImage, hsv_image, CV_RGB2HSV);
        cvSetImageCOI(hsv_image, 3);
        
        //get Average value
        CvScalar average = cvAvg(hsv_image, null);
        System.out.println("Average: " + average.val(0));

        //Compare to threshold
        
        //Send intensity to reasoning module
        
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
}
