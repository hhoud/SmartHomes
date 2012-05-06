/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package smarthomes.facedetection;

import java.util.HashMap;
import java.util.Map;
import static com.googlecode.javacv.cpp.opencv_core.*;
import static com.googlecode.javacv.cpp.opencv_objdetect.*;
import static com.googlecode.javacv.cpp.opencv_imgproc.*;

/**
 *
 * @author hh354
 */
public class EyeDetection {
    // The cascade definition to be used for detection.
    //private static final String CASCADE_FILE = "../../OpenCV-2.3.1/data/haarcascades/ojoD.xml";
    //private static final String CASCADE_FILE = "../../OpenCV-2.3.1/data/haarcascades/haarcascade_eye.xml";
    //private static final String CASCADE_FILE = "../../OpenCV-2.3.1/data/haarcascades/haarcascade_mcs_lefteye.xml";
    private static final String CASCADE_FILE = "../../OpenCV-2.3.1/data/haarcascades/haarcascade_mcs_righteye.xml";
            
    private IplImage grayImage, originalImage;
    private CvMemStorage storage;
    private Map<String,Object> result;
    private CvHaarClassifierCascade cascade;
    private CvSeq eyes;

    public Map<String,Object> detectEyes(IplImage origImage, boolean rgb){
        originalImage = origImage;
        
        // We need a grayscale image in order to do the recognition, so we
        // create a new image of the same size as the original one.
        grayImage = IplImage.create(originalImage.width(),
        originalImage.height(), IPL_DEPTH_8U, 1);

        // We convert the original image to grayscale.
        if(rgb)
            cvCvtColor(originalImage, grayImage, CV_BGR2GRAY);
        else
            grayImage = originalImage;

        storage = CvMemStorage.create();
        result = new HashMap<String, Object>();
        
        // We instantiate a classifier cascade to be used for detection, using the cascade definition.
        cascade = new CvHaarClassifierCascade(cvLoad(CASCADE_FILE));
        // We detect the eyes.
        eyes = cvHaarDetectObjects(grayImage, cascade, storage, 1.25, 3, CV_HAAR_DO_CANNY_PRUNING,cvSize(20, 20),cvSize(0, 0));
        
        //We iterate over the discovered eyes and draw yellow rectangles around them.
        for (int i = 0; i < eyes.total(); i++) {
          CvRect r = new CvRect(cvGetSeqElem(eyes, i));
          cvRectangle(originalImage, cvPoint(r.x(), r.y()),
          cvPoint(r.x() + r.width(), r.y() + r.height()), CvScalar.WHITE, 1, CV_AA, 0);
        }

        // Save the image to a new file.
        //cvSaveImage(args[1], originalImage);
        result.put("image",originalImage);
        result.put("hasFaces",(eyes.total() > 0)?true:false);
            
        return result;
    }

    public Map<String,Object> detectEyesTest(IplImage origImage, boolean rgb, double scale, int neighbours, int size){
        originalImage = origImage;
        
        // We need a grayscale image in order to do the recognition, so we
        // create a new image of the same size as the original one.
        grayImage = IplImage.create(originalImage.width(),
        originalImage.height(), IPL_DEPTH_8U, 1);

        // We convert the original image to grayscale.
        if(rgb)
            cvCvtColor(originalImage, grayImage, CV_BGR2GRAY);
        else
            grayImage = originalImage;

        storage = CvMemStorage.create();
        result = new HashMap<String, Object>();
        
        // We instantiate a classifier cascade to be used for detection, using the cascade definition.
        cascade = new CvHaarClassifierCascade(cvLoad(CASCADE_FILE));
        // We detect the eyes.
        eyes = cvHaarDetectObjects(grayImage, cascade, storage, scale, neighbours, CV_HAAR_DO_CANNY_PRUNING,cvSize(size, size),cvSize(0, 0));
        
        //We iterate over the discovered eyes and draw yellow rectangles around them.
        for (int i = 0; i < eyes.total(); i++) {
          CvRect r = new CvRect(cvGetSeqElem(eyes, i));
          cvRectangle(originalImage, cvPoint(r.x(), r.y()),
          cvPoint(r.x() + r.width(), r.y() + r.height()), CvScalar.WHITE, 1, CV_AA, 0);
        }

        // Save the image to a new file.
        //cvSaveImage(args[1], originalImage);
        result.put("image",originalImage);
        result.put("hasFaces",(eyes.total() > 0)?true:false);
            
        return result;
    }
}
