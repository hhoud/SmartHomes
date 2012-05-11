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

    private static final String CASCADE_FILE = "../../OpenCV-2.3.1/data/haarcascades/ojoD.xml";
    private IplImage grayImage, _originalImage;
    private CvMemStorage storage;
    private Map<String, Object> result;
    private CvHaarClassifierCascade cascade;
    private CvSeq eyes;
    private boolean _rgb;
    private IplImage iplCrop;

    public Map<String, Object> detectEyes(IplImage origImage, boolean rgb) {
        _originalImage = origImage;
        _rgb = rgb;

        // We need a grayscale image in order to do the recognition, so we
        // create a new image of the same size as the original one.
        grayImage = IplImage.create(_originalImage.width(),
                _originalImage.height(), IPL_DEPTH_8U, 1);

        // We convert the original image to grayscale.
        if (_rgb) {
            cvCvtColor(_originalImage, grayImage, CV_BGR2GRAY);
        } else {
            grayImage = _originalImage;
        }

        storage = CvMemStorage.create();
        result = new HashMap<String, Object>();

        // We instantiate a classifier cascade to be used for detection, using the cascade definition.
        cascade = new CvHaarClassifierCascade(cvLoad(CASCADE_FILE));
        // We detect the eyes.
        eyes = cvHaarDetectObjects(grayImage, cascade, storage, 1.05, 3, CV_HAAR_DO_CANNY_PRUNING, cvSize(0, 0), cvSize(0, 0));

        //Crop the image around the eye and send it through
        CvRect r = new CvRect(cvGetSeqElem(eyes, 0));
        iplCrop = cropImage(r);

        //Save the result
        result.put("image", _originalImage);
        result.put("hasFaces", (eyes.total() > 0) ? true : false);

        return result;
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
}
