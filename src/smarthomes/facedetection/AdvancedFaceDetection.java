/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package smarthomes.facedetection;

import java.util.Date;
import static com.googlecode.javacv.cpp.opencv_core.*;
import static com.googlecode.javacv.cpp.opencv_imgproc.*;
import static com.googlecode.javacv.cpp.opencv_objdetect.*;
import static com.googlecode.javacv.cpp.opencv_highgui.*;
import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author hh354
 */
public class AdvancedFaceDetection implements Runnable {
    
    // The cascade definition to be used for detection.
    private static final String CASCADE_FILE_FACES = "../../OpenCV-2.3.1/data/haarcascades/haarcascade_frontalface_alt.xml";
    //private static final String CASCADE_FILE_FACES = "../../OpenCV-2.3.1/data/haarcascades/haarcascade_frontalface_alt_tree.xml";
    private static final String CASCADE_FILE_EYES = "../../OpenCV-2.3.1/data/haarcascades/ojoD.xml";
    private static final String CASCADE_FILE_PROFILE_FACES = "../../OpenCV-2.3.1/data/haarcascades/haarcascade_profileface.xml";
    private IplImage grayImage, _originalImage;
    private CvMemStorage storage;
    private Map<String,Object> result;
    private boolean keepGoing, _rgb;
    private CvHaarClassifierCascade cascade;
    private CvSeq faces;
    private int counter, _user;
    
    public AdvancedFaceDetection(IplImage originalImage, boolean rgb, int user){
        _originalImage = originalImage;
        _rgb = rgb;
        _user = user;
    }
    
    public void detectFaces(){
        // We need a grayscale image in order to do the recognition, so we
        // create a new image of the same size as the original one.
        grayImage = IplImage.create(_originalImage.width(),
        _originalImage.height(), IPL_DEPTH_8U, 1);

        // We convert the original image to grayscale.
        if(_rgb)
            cvCvtColor(_originalImage, grayImage, CV_BGR2GRAY);
        else
            grayImage = _originalImage;

        storage = CvMemStorage.create();
        result = new HashMap<String, Object>();
        
        keepGoing = true;
        for(counter=0;counter<3;counter++){
            if(keepGoing){
                switch(counter){
                    case 0:
                        // We instantiate a classifier cascade to be used for detection, using the cascade definition.
                        cascade = new CvHaarClassifierCascade(cvLoad(CASCADE_FILE_FACES));
                        // We detect the faces.
                        faces = cvHaarDetectObjects(grayImage, cascade, storage, 1.1, 1, 0);
                        break;
                    case 1:
                        cascade = new CvHaarClassifierCascade(cvLoad(CASCADE_FILE_PROFILE_FACES));
                        faces = cvHaarDetectObjects(grayImage, cascade, storage, 1.1, 3, CV_HAAR_DO_CANNY_PRUNING);
                        break;
                    case 2:
                        cascade = new CvHaarClassifierCascade(cvLoad(CASCADE_FILE_EYES));
                        faces = cvHaarDetectObjects(grayImage, cascade, storage, 1.1, 30, CV_HAAR_DO_CANNY_PRUNING);
                        break;
                }
                runCascade();
            }
        }
    }
    
    private void runCascade(){
        //We iterate over the discovered faces and draw yellow rectangles around them.
        for (int i = 0; i < faces.total(); i++) {
            CvRect r = new CvRect(cvGetSeqElem(faces, i));
            cvRectangle(_originalImage, cvPoint(r.x(), r.y()),
            cvPoint(r.x() + r.width(), r.y() + r.height()), CvScalar.WHITE, 1, CV_AA, 0);
        }

        if(faces.total()>0 || counter == 2){
            Date date= new Date();
            cvSaveImage("faces/face_user_"+_user+"_"+date.getTime()+".jpg", _originalImage);
            keepGoing = false;
        }
    }

    @Override
    public void run() {
        detectFaces();
        return;
    }
}
