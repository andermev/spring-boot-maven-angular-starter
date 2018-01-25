package com.qwerty.app.images;

import com.amazonaws.services.rekognition.model.BoundingBox;
import com.amazonaws.services.rekognition.model.Image;
import org.opencv.core.Mat;
import org.opencv.core.MatOfByte;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.imgcodecs.Imgcodecs;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

public class AWSImageFaceDetectorAdapter implements ImageFaceDetector {

    private static final String EXTENSION = ".png";
    private AWSImageFaceDetector detector;

    public AWSImageFaceDetectorAdapter() {
        this.detector = new AWSImageFaceDetector();
    }

    public List<Rect> detectFaces(Mat image) {
        Image awsImage = toImage(image);
        List<BoundingBox> boxes = detector.detectFaces(awsImage);
        List<Rect> detections = new ArrayList<Rect>();
        for(BoundingBox box: boxes) {
            detections.add(toRect(box, image.width(), image.height()));
        }
        return detections;
    }

    private Image toImage(Mat image) {
        MatOfByte matOfBytes = new MatOfByte();
        Imgcodecs.imencode(EXTENSION, image, matOfBytes);

        byte[] bytes = matOfBytes.toArray();
        ByteBuffer buffer = ByteBuffer.wrap(bytes);
        return new Image().withBytes(buffer);
    }

    private Rect toRect(BoundingBox box, int width, int height) {
        float x1 = box.getLeft() * width;
        float x2 = (box.getLeft() + box.getWidth()) * width;
        float y1 = box.getTop() * height;
        float y2 = (box.getTop() + box.getHeight()) * height;

        x1 = x1 < 0 ? 0 : x1;
        y1 = y1 < 0 ? 0 : y1;
        x2 = x2 > width ? width : x2;
        y2 = y2 > height ? height : y2;

        Point point1 = new Point(x1, y1);
        Point point2 = new Point(x2, y2);
        return new Rect(point1, point2);
    }
}
