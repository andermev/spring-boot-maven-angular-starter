package com.qwerty.app.images;

import org.opencv.core.Mat;
import org.opencv.core.Rect;

import java.util.List;

public interface ImageFaceDetector {

    List<Rect> detectFaces(Mat image);

}
