package com.qwerty.app.images;

import org.opencv.core.Mat;
import org.opencv.core.Rect;
import org.opencv.core.Size;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;

import java.util.List;

public class ImageFaceBlur {

    private static final Integer SIGMA = 55;
    private ImageFaceDetector detector;

    public ImageFaceBlur(ImageFaceDetector detector) {
        this.detector = detector;
    }

    public void process(Mat image) {
        List<Rect> detections = detector.detectFaces(image);
        applyBlur(image, detections);
    }

    public void processAndSave(String input, String ouput) {
        Mat image = Imgcodecs.imread(input);
        process(image);
        Imgcodecs.imwrite(ouput, image);
    }

    private void applyBlur(Mat image, List<Rect> detections) {
        Size size = new Size(0, 0);
        for (Rect rect: detections) {
            Mat face = image.submat(rect);
            Imgproc.GaussianBlur(face, face, size, SIGMA, SIGMA);
        }
    }
}
