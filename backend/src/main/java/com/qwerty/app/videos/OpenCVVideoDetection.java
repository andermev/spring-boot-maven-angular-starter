package com.qwerty.app.videos;

import com.qwerty.app.images.ImageFaceBlur;
import org.opencv.core.Mat;
import org.opencv.core.Size;
import org.opencv.videoio.VideoCapture;
import org.opencv.videoio.VideoWriter;
import org.opencv.videoio.Videoio;

public class OpenCVVideoDetection {

    private ImageFaceBlur faceBlur;

    public OpenCVVideoDetection(ImageFaceBlur faceBlur) {
        this.faceBlur = faceBlur;
    }

    public void process(String input, String ouput) {
        VideoCapture capture = new VideoCapture(input);
        VideoWriter writer = getOutput(capture, ouput);

        Mat image = new Mat();
        while (capture.read(image)) {
            faceBlur.process(image);
            writer.write(image);
        }

        capture.release();
        writer.release();
    }

    private VideoWriter getOutput(VideoCapture capture, String output) {
        double height = capture.get(Videoio.CAP_PROP_FRAME_HEIGHT);
        double width = capture.get(Videoio.CAP_PROP_FRAME_WIDTH);
        Size size = new Size(width, height);

        int fourcc = VideoWriter.fourcc('M', 'J', 'P', 'G');
        return new VideoWriter(output, fourcc, 25.0, size);
    }
}
