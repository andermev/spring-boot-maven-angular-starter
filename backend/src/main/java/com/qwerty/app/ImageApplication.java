package com.qwerty.app;

import com.qwerty.app.images.AWSImageFaceDetectorAdapter;
import com.qwerty.app.images.ImageFaceBlur;
import com.qwerty.app.images.ImageFaceDetector;
import javafx.application.Application;
import org.opencv.core.Core;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class ImageApplication {

    private static final String FFMPEG_LIBRARY_NAME = "opencv_ffmpeg340_64";
    static {
        System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
        System.loadLibrary(FFMPEG_LIBRARY_NAME);
    }

    public static void main(String[] args) {
        ImageFaceDetector detector = new AWSImageFaceDetectorAdapter();
        ImageFaceBlur imageFaceBlur = new ImageFaceBlur(detector);
        imageFaceBlur.processAndSave("backend/input.jpg", "backend/ouput.jpg");

        //OpenCVVideoDetection detection = new OpenCVVideoDetection(imageFaceBlur);
        //detection.process("video.avi", "ouput.avi");
    }
}
