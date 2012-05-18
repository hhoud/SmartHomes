/****************************************************************************
 *                                                                           *
 *  OpenNI 1.x Alpha                                                         *
 *  Copyright (C) 2011 PrimeSense Ltd.                                       *
 *                                                                           *
 *  This file is part of OpenNI.                                             *
 *                                                                           *
 *  OpenNI is free software: you can redistribute it and/or modify           *
 *  it under the terms of the GNU Lesser General Public License as published *
 *  by the Free Software Foundation, either version 3 of the License, or     *
 *  (at your option) any later version.                                      *
 *                                                                           *
 *  OpenNI is distributed in the hope that it will be useful,                *
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of           *
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the             *
 *  GNU Lesser General Public License for more details.                      *
 *                                                                           *
 *  You should have received a copy of timport static com.googlecode.javacv.cpp.opencv_highgui.*;
import java.io.IOException;
he GNU Lesser General Public License *
 *  along with OpenNI. If not, see <http://www.gnu.org/licenses/>.           *
 *                                                                           *
 ****************************************************************************/
package smarthomes;

import com.googlecode.javacv.CanvasFrame;
import static com.googlecode.javacv.cpp.opencv_imgproc.*;
import static com.googlecode.javacv.cpp.opencv_core.*;
import static com.googlecode.javacv.cpp.opencv_highgui.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.OpenNI.*;

import java.nio.ShortBuffer;
import java.util.Map;
import java.util.Date;
import java.util.HashMap;
import java.awt.*;
import java.awt.color.ColorSpace;
import java.awt.image.*;
import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.ByteBuffer;
import javax.imageio.ImageIO;
import smarthomes.facedetection.AdvancedFaceDetection;
import smarthomes.facedetection.EyeDetection;
import smarthomes.facedetection.FaceDetection;

public class UserTracker extends Component {

    /**
     * @return the drawRGB
     */
    public boolean isDrawRGB() {
        return drawRGB;
    }

    class NewUserObserver implements IObserver<UserEventArgs> {

        @Override
        public void update(IObservable<UserEventArgs> observable,
                UserEventArgs args) {
            System.out.println("New user " + args.getId());
            try {
                if (skeletonCap.needPoseForCalibration()) {
                    poseDetectionCap.startPoseDetection(calibPose, args.getId());
                } else {
                    skeletonCap.requestSkeletonCalibration(args.getId(), true);
                }
            } catch (StatusException e) {
                e.printStackTrace();
            }
        }
    }

    class LostUserObserver implements IObserver<UserEventArgs> {

        @Override
        public void update(IObservable<UserEventArgs> observable,
                UserEventArgs args) {
            System.out.println("Lost user " + args.getId());
            joints.remove(args.getId());
        }
    }

    class CalibrationCompleteObserver implements IObserver<CalibrationProgressEventArgs> {

        @Override
        public void update(IObservable<CalibrationProgressEventArgs> observable,
                CalibrationProgressEventArgs args) {
            System.out.println("Calibraion complete: " + args.getStatus());
            try {
                if (args.getStatus() == CalibrationProgressStatus.OK) {
                    System.out.println("starting tracking " + args.getUser());
                    skeletonCap.startTracking(args.getUser());
                    joints.put(new Integer(args.getUser()), new HashMap<SkeletonJoint, SkeletonJointPosition>());
                } else if (args.getStatus() != CalibrationProgressStatus.MANUAL_ABORT) {
                    if (skeletonCap.needPoseForCalibration()) {
                        poseDetectionCap.startPoseDetection(calibPose, args.getUser());
                    } else {
                        skeletonCap.requestSkeletonCalibration(args.getUser(), true);
                    }
                }
            } catch (StatusException e) {
                e.printStackTrace();
            }
        }
    }

    class PoseDetectedObserver implements IObserver<PoseDetectionEventArgs> {

        @Override
        public void update(IObservable<PoseDetectionEventArgs> observable,
                PoseDetectionEventArgs args) {
            System.out.println("Pose " + args.getPose() + " detected for " + args.getUser());
            try {
                poseDetectionCap.stopPoseDetection(args.getUser());
                skeletonCap.requestSkeletonCalibration(args.getUser(), true);
            } catch (StatusException e) {
                e.printStackTrace();
            }
        }
    }
    /**
     * 
     */
    private static final long serialVersionUID = 1L;
    private OutArg<ScriptNode> scriptNode;
    private Context context;
    private DepthGenerator depthGen;
    private IRGenerator irGen;
    private ImageGenerator imgGen;
    private UserGenerator userGen;
    private DepthMetaData depthMD;
    private ImageMetaData imgMD;
    private IRMetaData irMD;
    private SkeletonCapability skeletonCap;
    private PoseDetectionCapability poseDetectionCap;
    private byte[] imgbytes;
    private float histogram[];
    String calibPose = null;
    HashMap<Integer, HashMap<SkeletonJoint, SkeletonJointPosition>> joints;
    private boolean drawBackground = false;
    private boolean drawPixels = true;
    private boolean drawSkeleton = true;
    private boolean drawRGB = true;
    private boolean printID = false;
    private boolean printState = false;
    private boolean highRes = false;
    private boolean mustFind = false;
    private String[] userCoordinates = new String[3];
    private BufferedImage bimg;
    private BufferedImage irImage = null;
    private MapOutputMode mapMode;
    private MapOutputMode imgMapMode;
    private IplImage iplRgbImage;
    private IplImage iplBgrImage;
    private Thread advFaceDetectionThread;
    private int width, height, left = 0, top = 0, right = 0, bottom = 0, imgWidth, imgHeight, counter, x = 0, y = 0;

    public UserTracker() {
        //setDrawRGB(drawRGB);
    }

    public void setDrawRGB(boolean drawRGB) {
        try {
            System.out.println("Drawing RGB: "+drawRGB);
            if (context != null) {
                context.stopGeneratingAll();
            }
            this.drawRGB = drawRGB;
            mapMode = new MapOutputMode(640, 480, 30);
            if (highRes) {
                imgMapMode = new MapOutputMode(1280, 1024, 15);
            } else {
                imgMapMode = new MapOutputMode(640, 480, 30);
            }
            context = new Context();
            License license = new License("PrimeSense", "0KOIk2JeIBYClPWVnMoRKn5cdY4=");
            context.addLicense(license);
            depthGen = DepthGenerator.create(context);
            depthGen.setMapOutputMode(mapMode);
            if (drawRGB) {
                imgGen = ImageGenerator.create(context);
                imgGen.setMapOutputMode(imgMapMode);
                imgGen.setPixelFormat(PixelFormat.RGB24);
            } else {
                irGen = IRGenerator.create(context);
                irGen.setMapOutputMode(imgMapMode);
            }
            context.setGlobalMirror(false);
            setObservers();
        } catch (GeneralException ex) {
            Logger.getLogger(UserTracker.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    /**
     * @return the mustFind
     */
    public boolean isMustFind() {
        return mustFind;
    }

    /**
     * @param mustFind the mustFind to set
     */
    public void setMustFind(boolean mustFind) {
        this.mustFind = mustFind;
    }

    private void setObservers() {
        try {
            DepthMetaData depthMD = depthGen.getMetaData();
            histogram = new float[10000];
            width = depthMD.getFullXRes();
            height = depthMD.getFullYRes();

            if (isDrawRGB()) {
                imgMD = imgGen.getMetaData();
                imgWidth = imgMD.getFullXRes();
                imgHeight = imgMD.getFullYRes();
            } else {
                irMD = irGen.getMetaData();
                imgWidth = irMD.getFullXRes();
                imgHeight = irMD.getFullYRes();
            }

            imgbytes = new byte[width * height * 3];

            userGen = UserGenerator.create(context);
            skeletonCap = userGen.getSkeletonCapability();
            poseDetectionCap = userGen.getPoseDetectionCapability();

            userGen.getNewUserEvent().addObserver(new NewUserObserver());
            userGen.getLostUserEvent().addObserver(new LostUserObserver());
            skeletonCap.getCalibrationCompleteEvent().addObserver(new CalibrationCompleteObserver());
            poseDetectionCap.getPoseDetectedEvent().addObserver(new PoseDetectedObserver());

            calibPose = skeletonCap.getSkeletonCalibrationPose();
            joints = new HashMap<Integer, HashMap<SkeletonJoint, SkeletonJointPosition>>();

            skeletonCap.setSkeletonProfile(SkeletonProfile.ALL);
            context.startGeneratingAll();
        } catch (GeneralException ex) {
            Logger.getLogger(UserTracker.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private void calcHist(ShortBuffer depth) {
        // reset
        for (int i = 0; i < histogram.length; ++i) {
            histogram[i] = 0;
        }

        depth.rewind();

        int points = 0;
        while (depth.remaining() > 0) {
            short depthVal = depth.get();
            if (depthVal != 0) {
                histogram[depthVal]++;
                points++;
            }
        }

        for (int i = 1; i < histogram.length; i++) {
            histogram[i] += histogram[i - 1];
        }

        if (points > 0) {
            for (int i = 1; i < histogram.length; i++) {
                histogram[i] = 1.0f - (histogram[i] / (float) points);
            }
        }
    }
    private static final int MIN_8_BIT = 0;
    private static final int MAX_8_BIT = 255;
    // for mapping the IR values into a 8-bit range

    private BufferedImage createGrayIm(ShortBuffer irSB, int minIR, int maxIR) {
        // create a grayscale image
        BufferedImage image = new BufferedImage(imgWidth, imgHeight, BufferedImage.TYPE_BYTE_GRAY);
        // access the image's data buffer
        byte[] data = ((DataBufferByte) image.getRaster().getDataBuffer()).getData();
        float displayRatio = (float) (MAX_8_BIT - MIN_8_BIT) / (maxIR - minIR);
        // scale the converted IR data over the grayscale range;
        int i = 0;
        while (irSB.remaining() > 0) {
            int irVal = irSB.get();
            int out;
            if (irVal <= minIR) {
                out = MIN_8_BIT;
            } else if (irVal >= maxIR) {
                out = MAX_8_BIT;
            } else {
                out = (int) ((irVal - minIR) * displayRatio);
            }
            data[i++] = (byte) out;
            // store in the data buffer
        }
        return image;
    } // end of createGrayIm()

    public void updateIR() {
        if (!isDrawRGB()) {
            System.out.println("Updating IR");
            try {
                if (irGen != null) {
                    ShortBuffer irSB = irGen.getIRMap().createShortBuffer();
                    // scan the IR data, storing the min and max values
                    int minIR = irSB.get();
                    int maxIR = minIR;
                    while (irSB.remaining() > 0) {
                        int irVal = irSB.get();
                        if (irVal > maxIR) {
                            maxIR = irVal;
                        }
                        if (irVal < minIR) {
                            minIR = irVal;
                        }
                    }
                    irSB.rewind();
                    // convert the IR values into 8-bit grayscales
                    irImage = createGrayIm(irSB, minIR, maxIR);
                }
            } catch (GeneralException e) {
                System.out.println(e);
            }
        }
    } // end of updateIRImage()

    void updateDepth() {
        try {
            //context.waitAnyUpdateAll();
            context.waitNoneUpdateAll();

            DepthMetaData depthMD = depthGen.getMetaData();
            SceneMetaData sceneMD = userGen.getUserPixels(0);

            ShortBuffer scene = sceneMD.getData().createShortBuffer();
            ShortBuffer depth = depthMD.getData().createShortBuffer();
            calcHist(depth);
            depth.rewind();

            while (depth.remaining() > 0) {
                int pos = depth.position();
                short pixel = depth.get();
                short user = scene.get();

                imgbytes[3 * pos] = 0;
                imgbytes[3 * pos + 1] = 0;
                imgbytes[3 * pos + 2] = 0;

                if (drawBackground || pixel != 0) {
                    int colorID = user % (colors.length - 1);
                    if (user == 0) {
                        colorID = colors.length - 1;
                    }
                    if (pixel != 0) {
                        float histValue = histogram[pixel];
                        imgbytes[3 * pos] = (byte) (histValue * colors[colorID].getRed());
                        imgbytes[3 * pos + 1] = (byte) (histValue * colors[colorID].getGreen());
                        imgbytes[3 * pos + 2] = (byte) (histValue * colors[colorID].getBlue());
                    }
                }
            }
        } catch (GeneralException e) {
            e.printStackTrace();
        }
    }

    public Dimension getPreferredSize() {
        return new Dimension(width, height);
    }
    Color colors[] = {Color.RED, Color.BLUE, Color.CYAN, Color.GREEN, Color.MAGENTA, Color.PINK, Color.YELLOW, Color.WHITE};

    public void getJoint(int user, SkeletonJoint joint) throws StatusException {
        SkeletonJointPosition pos = skeletonCap.getSkeletonJointPosition(user, joint);
        if (pos.getPosition().getZ() != 0) {
            joints.get(user).put(joint, new SkeletonJointPosition(depthGen.convertRealWorldToProjective(pos.getPosition()), pos.getConfidence()));
        } else {
            joints.get(user).put(joint, new SkeletonJointPosition(new Point3D(), 0));
        }
    }

    public void getJoints(int user) throws StatusException {
        getJoint(user, SkeletonJoint.HEAD);
        getJoint(user, SkeletonJoint.NECK);

        getJoint(user, SkeletonJoint.LEFT_SHOULDER);
        getJoint(user, SkeletonJoint.LEFT_ELBOW);
        getJoint(user, SkeletonJoint.LEFT_HAND);

        getJoint(user, SkeletonJoint.RIGHT_SHOULDER);
        getJoint(user, SkeletonJoint.RIGHT_ELBOW);
        getJoint(user, SkeletonJoint.RIGHT_HAND);

        getJoint(user, SkeletonJoint.TORSO);

        getJoint(user, SkeletonJoint.LEFT_HIP);
        getJoint(user, SkeletonJoint.LEFT_KNEE);
        getJoint(user, SkeletonJoint.LEFT_FOOT);

        getJoint(user, SkeletonJoint.RIGHT_HIP);
        getJoint(user, SkeletonJoint.RIGHT_KNEE);
        getJoint(user, SkeletonJoint.RIGHT_FOOT);

    }

    void drawLine(Graphics g, HashMap<SkeletonJoint, SkeletonJointPosition> jointHash, SkeletonJoint joint1, SkeletonJoint joint2) {
        Point3D pos1 = jointHash.get(joint1).getPosition();
        Point3D pos2 = jointHash.get(joint2).getPosition();

        if (jointHash.get(joint1).getConfidence() == 0 || jointHash.get(joint2).getConfidence() == 0) {
            return;
        }

        g.drawLine((int) pos1.getX(), (int) pos1.getY(), (int) pos2.getX(), (int) pos2.getY());
    }

    public void drawSkeleton(Graphics g, int user) throws StatusException {
        getJoints(user);
        HashMap<SkeletonJoint, SkeletonJointPosition> dict = joints.get(new Integer(user));

        //Send person present to reasoning module
        EnvironmentVars.getInstance().setPersonPresent(true);

        //Send coordinates to reasoning module
        SkeletonJointPosition headPos = joints.get(new Integer(user)).get(SkeletonJoint.HEAD);
        int x = (int) headPos.getPosition().getX();
        int y = (int) headPos.getPosition().getY();
        int z = (int) headPos.getPosition().getZ();
        EnvironmentVars.getInstance().setPersonPosition(new int[]{x, y, z});

        /*drawLine(g, dict, SkeletonJoint.HEAD, SkeletonJoint.NECK);
        
        drawLine(g, dict, SkeletonJoint.LEFT_SHOULDER, SkeletonJoint.TORSO);
        drawLine(g, dict, SkeletonJoint.RIGHT_SHOULDER, SkeletonJoint.TORSO);
        
        drawLine(g, dict, SkeletonJoint.NECK, SkeletonJoint.LEFT_SHOULDER);
        drawLine(g, dict, SkeletonJoint.LEFT_SHOULDER, SkeletonJoint.LEFT_ELBOW);
        drawLine(g, dict, SkeletonJoint.LEFT_ELBOW, SkeletonJoint.LEFT_HAND);
        
        drawLine(g, dict, SkeletonJoint.NECK, SkeletonJoint.RIGHT_SHOULDER);
        drawLine(g, dict, SkeletonJoint.RIGHT_SHOULDER, SkeletonJoint.RIGHT_ELBOW);
        drawLine(g, dict, SkeletonJoint.RIGHT_ELBOW, SkeletonJoint.RIGHT_HAND);
        
        drawLine(g, dict, SkeletonJoint.LEFT_HIP, SkeletonJoint.TORSO);
        drawLine(g, dict, SkeletonJoint.RIGHT_HIP, SkeletonJoint.TORSO);
        drawLine(g, dict, SkeletonJoint.LEFT_HIP, SkeletonJoint.RIGHT_HIP);
        
        drawLine(g, dict, SkeletonJoint.LEFT_HIP, SkeletonJoint.LEFT_KNEE);
        drawLine(g, dict, SkeletonJoint.LEFT_KNEE, SkeletonJoint.LEFT_FOOT);
        
        drawLine(g, dict, SkeletonJoint.RIGHT_HIP, SkeletonJoint.RIGHT_KNEE);
        drawLine(g, dict, SkeletonJoint.RIGHT_KNEE, SkeletonJoint.RIGHT_FOOT);*/
    }

    private void writeToFile(String text) {
        BufferedWriter out = null;
        try {
            out = new BufferedWriter(new FileWriter("cropping_time.txt", true));
            out.write(text + ";");
            out.newLine();
        } catch (IOException ex) {
            Logger.getLogger(UserTracker.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                out.close();
            } catch (IOException ex) {
                Logger.getLogger(UserTracker.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    private void analyseImage(IplImage iplCrop, int user) {
        Date date = new Date();

        //Perform automatic gamma correction
        GammaCorrection gc = new GammaCorrection();
        iplCrop = gc.correctGamma(iplCrop, isDrawRGB());

        //For the IR image we smooth the image to remove the dot pattern of the laser
        if (!isDrawRGB()) {
            cvSmooth(iplCrop, iplCrop, CV_MEDIAN, 5);
        }

        boolean hasFace = false;
        Map<String, Object> result = null;

        //Standard: Run eye detection algorithm
        //Must find: Run advanced face detection algorithm
        if (mustFind) {
            AdvancedFaceDetection fdetect = new AdvancedFaceDetection();
            result = fdetect.detectFaces(iplCrop, isDrawRGB());
        } else {
            if(isDrawRGB()){
                EyeDetection edetect = new EyeDetection();
                result = edetect.detectEyes(iplRgbImage, isDrawRGB());
            }else{
                FaceDetection fdetect = new FaceDetection();
                result = fdetect.detectFaces(iplRgbImage, isDrawRGB());
            }
        }

        iplCrop = (IplImage) result.get("image");
        hasFace = (Boolean) result.get("hasFaces");

        if (hasFace) {
            try {
                //Send the picture to the reasoning module
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                ImageIO.write(iplCrop.getBufferedImage(), "jpg", baos);
                byte[] imageArray = baos.toByteArray();
                EnvironmentVars.getInstance().setDetectedFace(imageArray);
            } catch (IOException ex) {
                Logger.getLogger(UserTracker.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    private void cropImage(int cTop, int cLeft, int cWidth, int cHeight, int user) {
        try {
            // create destination image
            IplImage iplCrop;
            if (isDrawRGB()) {
                iplCrop = IplImage.create(cWidth, cHeight, IPL_DEPTH_8U, 3);
            } else {
                iplCrop = IplImage.create(cWidth, cHeight, IPL_DEPTH_8U, 1);
            }

            //choose between ir or rgb
            IplImage image = (isDrawRGB()) ? iplBgrImage : new IplImage().createFrom(irImage);

            //Set ROI
            cvSetImageROI(image, cvRect(cLeft, cTop, cWidth, cHeight));

            /* copy subimage */
            cvCopy(image, iplCrop);

            /* reset the Region of Interest */
            cvResetImageROI(image);

            analyseImage(iplCrop, user);
        } catch (Exception ex) {
            Logger.getLogger(UserTracker.class.getName()).log(Level.SEVERE, null, ex + "left:" + left + ",top:" + top + ",width:" + cWidth + ",height:" + cHeight);
        }
    }

    private void trackUser(Graphics g, int user) throws StatusException {
        DepthMetaData depthMD = depthGen.getMetaData();
        SceneMetaData sceneMD = userGen.getUserPixels(0);
        SceneMap sceneMap = sceneMD.getData();
        //int[][] userMap = new int[sceneMap.getXRes()][sceneMap.getYRes()];
        int maxX = 0;
        int minX = (highRes) ? imgWidth : width;
        int maxY = 0;
        int minY = (highRes) ? imgHeight : height;
        int edgeX = minX;
        int edgeY = minY;

        //find the user region
        for (int j = 0; j <= sceneMap.getYRes() - 1; j++) {
            for (int i = 0; i <= sceneMap.getXRes() - 1; i++) {
                if (sceneMap.readPixel(i, j) == user) {
                    if (i > maxX || i == edgeX) {
                        maxX = i;
                    }
                    if (j > maxY || j == edgeY) {
                        maxY = j;
                    }

                    if (i < minX || i == 0) {
                        minX = i;
                    }
                    if (j < minY || j == 0) {
                        minY = j;
                    }
                }
            }
        }
        cropImage(minY, minX, maxX - minX, maxY - minY, user);
    }

    private void trackHead(Graphics g, int user) throws StatusException {
        SceneMetaData sceneMD = userGen.getUserPixels(0);
        SceneMap sceneMap = sceneMD.getData();

        //find the head and torso positions
        SkeletonJointPosition headPos = joints.get(new Integer(user)).get(SkeletonJoint.HEAD);
        SkeletonJointPosition neckPos = joints.get(new Integer(user)).get(SkeletonJoint.TORSO);
        Point3D headPoint = headPos.getPosition();
        Point3D neckPoint = neckPos.getPosition();

        int threshold = 10;
        //if the position of the head varies too much from previous position, calculate a new crop region
        if (x < headPoint.getX() - threshold || x > headPoint.getX() + threshold || y < headPoint.getY() - threshold || y > headPoint.getY() + threshold) {
            x = (int) headPoint.getX();
            y = (int) headPoint.getY();

            //define region around head
            //define top of head starting from head bone position
            top = y;
            boolean userPix = true;
            while (userPix) {
                if (sceneMap.readPixel(x, top) == new Integer(user) && top > 0) {
                    top--;
                } else {
                    userPix = false;
                }
            }
            //define left of head starting from head bone position
            left = x;
            userPix = true;
            while (userPix) {
                if (sceneMap.readPixel(left, y) == new Integer(user) && left > 0) {
                    left--;
                } else {
                    userPix = false;
                }
            }
            //define right of head starting from head bone position
            right = x;
            userPix = true;
            while (userPix) {
                if (sceneMap.readPixel(right, y) == new Integer(user) && right < sceneMap.getXRes()) {
                    right++;
                } else {
                    userPix = false;
                }
            }
            //bottom = (int) depthGen.convertRealWorldToProjective(neckPoint).getY();
            bottom = (int) neckPoint.getY();
            bottom = (bottom < 0) ? 0 : bottom;
        }

        if (highRes) {
            cropImage(2 * (top), 2 * (left - 20), 2 * (right - left + 30), 2 * (bottom - top - 30), user);
        } else {
            cropImage((top + 15), (left - 20), (right - left + 30), (bottom - top - 30), user);
        }
    }

    private void histogramAnalysis() {
        CvScalar average = new CvScalar();
        //convert to hsv
        if (isDrawRGB()) {
            IplImage hsv_image = IplImage.create(imgWidth, imgHeight, IPL_DEPTH_8U, 3);
            cvCvtColor(iplBgrImage, hsv_image, CV_RGB2HSV);
            cvSetImageCOI(hsv_image, 3);
            //get Average intensity
            average = cvAvg(hsv_image, null);
        } else {
            if(irImage != null)
                average = cvAvg(IplImage.createFrom(irImage), null);
        }

        //Send intensity value to reasoning module
        if (average != null) {
            EnvironmentVars.getInstance().setHistIntensity(average.val(0));
        }
    }
    CanvasFrame canvas = new CanvasFrame("RGB / IR");

    public void paint(Graphics g) {
        counter++;
        if (drawPixels) {
            DataBufferByte dataBuffer = new DataBufferByte(imgbytes, width * height * 3);
            WritableRaster raster = Raster.createInterleavedRaster(dataBuffer, width, height, width * 3, 3, new int[]{0, 1, 2}, null);
            ColorModel colorModel = new ComponentColorModel(ColorSpace.getInstance(ColorSpace.CS_sRGB), new int[]{8, 8, 8}, false, false, ComponentColorModel.OPAQUE, DataBuffer.TYPE_BYTE);
            bimg = new BufferedImage(colorModel, raster, false, null);
            g.drawImage(bimg, 0, 0, null);
        }

        if (isDrawRGB()) {
            try {
                // create image
                iplRgbImage = IplImage.create(imgWidth, imgHeight, IPL_DEPTH_8U, 3);
                iplBgrImage = IplImage.create(imgWidth, imgHeight, IPL_DEPTH_8U, 3);
                // fill image
                ImageMap imageMap = imgGen.getImageMap();
                ByteBuffer byteBuffer = iplRgbImage.getByteBuffer();
                long ptr = imageMap.getNativePtr();
                NativeAccess.copyToBuffer(byteBuffer, ptr, imgWidth * imgHeight * 3);
                //Convert image color
                cvCvtColor(iplRgbImage, iplBgrImage, CV_RGB2BGR);
                canvas.showImage(iplBgrImage);
                byteBuffer.rewind();

            } catch (GeneralException ex) {
                Logger.getLogger(UserTracker.class.getName()).log(Level.SEVERE, null, ex);
            }
        } else {
            canvas.showImage(irImage);
        }

        try {
            //User cropping/analysis
            int[] users = userGen.getUsers();
            if (users.length == 0 && mustFind) {
                //no user found but system says there is one -> force analysis on whole scene
                analyseImage(iplBgrImage, 0);
            } else {
                for (int i = 0; i < users.length; ++i) {
                    //set the color of the user in the depth image
                    Color c = colors[users[i] % colors.length];
                    c = new Color(255 - c.getRed(), 255 - c.getGreen(), 255 - c.getBlue());

                    g.setColor(c);
                    //every 15 frames (= every half second) we check the user
                    if (counter % 15 == 0) {
                        if (drawSkeleton && skeletonCap.isSkeletonTracking(users[i])) {
                            //get positions of all the joints
                            drawSkeleton(g, users[i]);
                            trackHead(g, users[i]);
                        } else {
                            trackUser(g, users[i]);
                        }
                    }
                }
            }

            //histogram analysis
            //histogramAnalysis();

        } catch (StatusException e) {
            e.printStackTrace();
        }
    }
}
