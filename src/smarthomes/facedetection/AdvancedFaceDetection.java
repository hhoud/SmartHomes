/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package smarthomes.facedetection;

import static com.googlecode.javacv.cpp.opencv_core.*;
import static com.googlecode.javacv.cpp.opencv_imgproc.*;
import static com.googlecode.javacv.cpp.opencv_objdetect.*;
import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author hh354
 */
public class AdvancedFaceDetection {

    // The cascade definition to be used for detection.
    private static final String CASCADE_FILE_FACES = "../../OpenCV-2.3.1/data/haarcascades/haarcascade_frontalface_alt2.xml";
    private static final String CASCADE_FILE_EYES = "../../OpenCV-2.3.1/data/haarcascades/ojoD.xml";
    private static final String CASCADE_FILE_PROFILE_FACES = "../../OpenCV-2.3.1/data/haarcascades/haarcascade_profileface.xml";
    private IplImage grayImage, _originalImage, iplCrop;
    private CvMemStorage storage;
    private Map<String, Object> result;
    private boolean keepGoing, _rgb;
    private CvHaarClassifierCascade cascade;
    private CvSeq faces;
    private CvSeq eyes;
    private int counter, _user;

    public Map<String, Object> detectFaces(IplImage originalImage, boolean rgb) {
        _rgb = rgb;
        _originalImage = originalImage;
        
        // We need a grayscale image in order to do the recognition, so we
        // create a new image of the same size as the original one.
        grayImage = IplImage.create(_originalImage.width(), _originalImage.height(), IPL_DEPTH_8U, 1);
        
        // We convert the original image to grayscale.
        if (rgb) {
            cvCvtColor(_originalImage, grayImage, CV_BGR2GRAY);
        } else {
            grayImage = _originalImage;
        }

        storage = CvMemStorage.create();
        result = new HashMap<String, Object>(){
            {
                put("image",null);
                put("hasFaces",false);
            }};

        // We instantiate a classifier cascade to be used for detection, using the cascade definition.
        cascade = new CvHaarClassifierCascade(cvLoad(CASCADE_FILE_PROFILE_FACES));
        // We detect the faces.
        faces = cvHaarDetectObjects(grayImage, cascade, storage, 1.15, 1, CV_HAAR_DO_CANNY_PRUNING, cvSize(40, 40), cvSize(0, 0));

        //if the frontal cascade doesn't find a face, try with the profile cascade
        if (faces.total() < 1) {
            cascade = new CvHaarClassifierCascade(cvLoad(CASCADE_FILE_FACES));
            faces = cvHaarDetectObjects(grayImage, cascade, storage, 1.05, 1, CV_HAAR_DO_CANNY_PRUNING, cvSize(40, 40), cvSize(0, 0));
        }

        //find eyes
        cascade = new CvHaarClassifierCascade(cvLoad(CASCADE_FILE_EYES));
        eyes = cvHaarDetectObjects(grayImage, cascade, storage, 1.05, 3, CV_HAAR_DO_CANNY_PRUNING, cvSize(0, 0), cvSize(0, 0));

        if (eyes.total() > 0 && faces.total() > 0) {
            //find possible overlaps
            CvRect detection = runCascade();

            if (detection != null) {
                iplCrop = cropImage(detection);
                result.put("image", iplCrop);
                result.put("hasFaces", (faces.total() > 0) ? true : false);
            }else{
                findApproximate();
            }
        } else{
            findApproximate();
        }

        return result;
    }

    private void findApproximate(){
        if (faces.total() > 0) {
            CvRect r = new CvRect(cvGetSeqElem(faces, 0));
            iplCrop = cropImage(r);
            result.put("image", iplCrop);
            result.put("hasFaces", (faces.total() > 0) ? true : false);
        }
        if (eyes.total() > 0) {
            CvRect r = new CvRect(cvGetSeqElem(eyes, 0));
            iplCrop = cropImage(r);
            result.put("image", iplCrop);
            result.put("hasFaces", (eyes.total() > 0) ? true : false);
        }
    }


    private IplImage cropImage(CvRect detection) {
        if (_rgb) {
            iplCrop = IplImage.create(detection.width(), detection.height(), IPL_DEPTH_8U, 3);
        } else {
            iplCrop = IplImage.create(detection.width(), detection.height(), IPL_DEPTH_8U, 1);
        }
        //Set ROI
        cvSetImageROI(_originalImage, detection);

        /* copy subimage */
        cvCopy(_originalImage, iplCrop);
        
        return iplCrop;
    }

    private CvRect runCascade() {
        //We iterate over the discovered faces and eyes and find intersections.
        for (int i = 0; i < faces.total(); i++) {
            for (int j = 0; j < eyes.total(); j++) {
                CvRect face = new CvRect(cvGetSeqElem(faces, i));
                CvRect eye = new CvRect(cvGetSeqElem(eyes, j));
                CvRect overlap = intersect(face, eye);
                if (overlap != null) {
                    return overlap;
                }
            }
        }
        return null;
    }

    private CvRect intersect(CvRect r1, CvRect r2) {
        CvRect intersection = new CvRect();

        // find overlapping region
        intersection.x((r1.x() < r2.x()) ? r2.x() : r1.x());
        intersection.y((r1.y() < r2.y()) ? r2.y() : r1.y());
        intersection.width((r1.x() + r1.width() < r2.x() + r2.width()) ? r1.x() + r1.width() : r2.x() + r2.width());
        intersection.width(intersection.width() - intersection.x());
        intersection.height((r1.y() + r1.height() < r2.y() + r2.height()) ? r1.y() + r1.height() : r2.y() + r2.height());
        intersection.height(intersection.height() - intersection.y());

        // check for non-overlapping regions
        if ((intersection.width() <= 0) || (intersection.height() <= 0)) {
            return null;
        } else {
            return intersection;
        }
    }
}
