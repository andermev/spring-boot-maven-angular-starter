package com.qwerty.app.images;

import org.opencv.core.Mat;
import org.opencv.core.MatOfRect;
import org.opencv.core.Rect;
import org.opencv.objdetect.CascadeClassifier;

import java.util.List;

public class OpenCVImageFaceDetector implements ImageFaceDetector {

    private static final String HAAR_CASCADE_FILE = "haarcascade_frontalface_alt.xml";
    private CascadeClassifier faceDetector;

    public OpenCVImageFaceDetector() {
        faceDetector = new CascadeClassifier(HAAR_CASCADE_FILE);
    }

    public List<Rect> detectFaces(Mat image) {
        MatOfRect detections = new MatOfRect();
        faceDetector.detectMultiScale(image, detections);
        return detections.toList();
    }

}
