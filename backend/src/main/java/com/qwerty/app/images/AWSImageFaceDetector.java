package com.qwerty.app.images;

import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.rekognition.AmazonRekognition;
import com.amazonaws.services.rekognition.AmazonRekognitionClientBuilder;
import com.amazonaws.services.rekognition.model.*;

import java.util.ArrayList;
import java.util.List;

public class AWSImageFaceDetector {

    private static final String ACCESS_KEY = "AKIAJXUMWQHM47NKPY3A";
    private static final String SECRET_KEY = "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";
    private static final String REGION = "eu-west-1";
    private AmazonRekognition client;

    public AWSImageFaceDetector() {
        client = AmazonRekognitionClientBuilder
                .standard()
                .withRegion(REGION)
                .withCredentials(new AWSStaticCredentialsProvider(
                        new BasicAWSCredentials(ACCESS_KEY, SECRET_KEY)))
                .build();
    }

    public List<BoundingBox> detectFaces(Image image) {
        DetectFacesRequest request = new DetectFacesRequest()
                .withImage(image);
        DetectFacesResult result = client.detectFaces(request);
        List<FaceDetail> faceDetails = result.getFaceDetails();

        List<BoundingBox> detections = new ArrayList<BoundingBox>();
        for(FaceDetail faceDetail: faceDetails) {
            detections.add(faceDetail.getBoundingBox());
        }
        return detections;
    }
}
