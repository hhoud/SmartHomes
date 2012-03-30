/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package smarthomes.facedetection;

import java.util.HashMap;
import java.util.Map;
import static com.googlecode.javacv.cpp.opencv_core.*;
import static com.googlecode.javacv.cpp.opencv_imgproc.*;
import static com.googlecode.javacv.cpp.opencv_objdetect.*;

public class FaceDetection {
 
    // The cascade definition to be used for detection.
    private static final String CASCADE_FILE_FACES = "../../OpenCV-2.3.1/data/haarcascades/haarcascade_frontalface_alt_tree.xml";
    private IplImage grayImage, originalImage;
    private CvMemStorage storage;
    private Map<String,Object> result;
    private CvHaarClassifierCascade cascade;
    private CvSeq faces;

    public static void main(String[] args) throws Exception {
    }

    public Map<String,Object> detectFaces(IplImage origImage, boolean rgb){
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
        cascade = new CvHaarClassifierCascade(cvLoad(CASCADE_FILE_FACES));
        // We detect the faces.
        faces = cvHaarDetectObjects(grayImage, cascade, storage, 1.25, 3, CV_HAAR_DO_CANNY_PRUNING);
        
        //We iterate over the discovered faces and draw yellow rectangles around them.
        for (int i = 0; i < faces.total(); i++) {
          CvRect r = new CvRect(cvGetSeqElem(faces, i));
          cvRectangle(originalImage, cvPoint(r.x(), r.y()),
          cvPoint(r.x() + r.width(), r.y() + r.height()), CvScalar.WHITE, 1, CV_AA, 0);
        }

        // Save the image to a new file.
        //cvSaveImage(args[1], originalImage);
        result.put("image",originalImage);
        result.put("hasFaces",(faces.total() > 0)?true:false);
            
        return result;
    }
  
}