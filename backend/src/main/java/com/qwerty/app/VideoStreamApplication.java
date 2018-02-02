package com.qwerty.app;

import org.springframework.boot.autoconfigure.SpringBootApplication;

import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.AWSCredentialsProvider;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.profile.ProfileCredentialsProvider;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.rekognition.AmazonRekognition;
import com.amazonaws.services.rekognition.AmazonRekognitionClientBuilder;
import com.amazonaws.services.rekognition.model.CreateCollectionRequest;
import com.amazonaws.services.rekognition.model.CreateCollectionResult;
import com.amazonaws.services.rekognition.model.CreateStreamProcessorRequest;
import com.amazonaws.services.rekognition.model.CreateStreamProcessorResult;
import com.amazonaws.services.rekognition.model.FaceSearchSettings;
import com.amazonaws.services.rekognition.model.Image;
import com.amazonaws.services.rekognition.model.IndexFacesRequest;
import com.amazonaws.services.rekognition.model.IndexFacesResult;
import com.amazonaws.services.rekognition.model.KinesisDataStream;
import com.amazonaws.services.rekognition.model.KinesisVideoStream;
import com.amazonaws.services.rekognition.model.S3Object;
import com.amazonaws.services.rekognition.model.StartStreamProcessorRequest;
import com.amazonaws.services.rekognition.model.StartStreamProcessorResult;
import com.amazonaws.services.rekognition.model.StreamProcessorInput;
import com.amazonaws.services.rekognition.model.StreamProcessorOutput;
import com.amazonaws.services.rekognition.model.StreamProcessorSettings;

@SpringBootApplication
public class VideoStreamApplication {

    private static final String PROFILE_NAME = "hackaton";
    private static final String COLLECTION_ID = "FaceCollection2";
    private static final String ALL_DETECTION_ATTRIBUTES = "ALL";
    private static final String S3_IMAGE_NAME = "some_face.jpg";
    private static final String BUCKET = "face-bucket";
    private static final String VIDEO_STREAM_NAME = "victorv-stream";
    private static final String STREAM_PROCESSOR_NAME = "RekognitionStreamProcessor";
    private static final String KINESIS_VIDEO_STREAM_ARN = "arn:aws:kinesisvideo:us-west-2:292145859847:stream/victorv-stream/1512083887959";
    private static final String KINESIS_DATA_STREAM_ARN = "arn:aws:kinesis:us-west-2:292145859847:stream/qwerty-datastream";
    private static final String ROLE_ARN = "arn:aws:iam::292145859847:role/service-role/lamba-hackathon-image-rekognition";

    public static void main(String[] args) throws Exception {
//        AmazonRekognition amazonRekognition = getAmazonRekognitionCredentials();
//        String collectionId = getFaceCollection(amazonRekognition);
////        addFaceToCollection(amazonRekognition);
//        createKinesisVideoStream();
//
//        //5. Create a data stream
//        //This is done from the console. Can't be done with AWS cli or JDK.
//
//        String streamProcessorName = createAndStartStreamProcessor(amazonRekognition);
//        startStreamProcessor(amazonRekognition, streamProcessorName);

        //8. Use PutMedia to place the source video into the KinesisVideoStream
        //This can also be done with the higher level API. Here are the two samples:
        //(https://docs.aws.amazon.com/kinesisvideostreams/latest/dg/producer-sdk-javaapi.html)
    }

    private static void startStreamProcessor(AmazonRekognition amazonRekognition, String streamProcessorName) {
        //7. Start the stream processor
        StartStreamProcessorResult startStreamProcessorResult =
                amazonRekognition
                        .startStreamProcessor(new StartStreamProcessorRequest()
                                .withName(streamProcessorName));

        StartStreamProcessorResult startStreamProcessorResult2 = startStreamProcessorResult;
    }

    private static String createAndStartStreamProcessor(AmazonRekognition amazonRekognition) {
        //6. Create and Start stream processor
        String streamProcessorName = STREAM_PROCESSOR_NAME;
        String kinesisVideoStreamArn = KINESIS_VIDEO_STREAM_ARN; //These ARNs must be obtained from the console.
        String kinesisDataStreamArn = KINESIS_DATA_STREAM_ARN;
        float matchThreshold = 0.9f;

        KinesisVideoStream kinesisVideoStream = new KinesisVideoStream().withArn(kinesisVideoStreamArn);
        StreamProcessorInput streamProcessorInput =
                new StreamProcessorInput().withKinesisVideoStream(kinesisVideoStream);
        KinesisDataStream kinesisDataStream = new KinesisDataStream().withArn(kinesisDataStreamArn);
        StreamProcessorOutput streamProcessorOutput =
                new StreamProcessorOutput().withKinesisDataStream(kinesisDataStream);
        FaceSearchSettings faceSearchSettings =
                new FaceSearchSettings()
                        .withCollectionId(COLLECTION_ID)
                        .withFaceMatchThreshold(matchThreshold);
        StreamProcessorSettings streamProcessorSettings =
                new StreamProcessorSettings()
                        .withFaceSearch(faceSearchSettings)
                ;

        CreateStreamProcessorResult createStreamProcessorResult = amazonRekognition
                .createStreamProcessor(new CreateStreamProcessorRequest()
                        .withInput(streamProcessorInput)
                        .withOutput(streamProcessorOutput)
                        .withRoleArn(ROLE_ARN)
                        .withSettings(streamProcessorSettings)
                        .withName(STREAM_PROCESSOR_NAME));
        return streamProcessorName;
    }

    private static void createKinesisVideoStream() {
        //4. Create a Kinesis video stream (Kinesis es otra librería diferente)
        //aws kinesisvideo create-stream --stream-name "MyKinesisVideoStream" --data-retention-in-hours "24"
        String videoStreamName = VIDEO_STREAM_NAME;
    }

    private static void addFaceToCollection(AmazonRekognition amazonRekognition) {
        //3. Add a face to the collection
        String bucket = BUCKET;
        String s3ImageName = S3_IMAGE_NAME; //Must be placed in the S3 bucket.
        Image image=new Image()
                .withS3Object(new S3Object()
                        .withBucket(bucket)
                        .withName(s3ImageName));

        IndexFacesRequest indexFacesRequest = new IndexFacesRequest()
                .withImage(image)
//                .withCollectionId(collectionId)
                .withExternalImageId(s3ImageName)
                .withDetectionAttributes(ALL_DETECTION_ATTRIBUTES);

        IndexFacesResult indexFacesResult = amazonRekognition.indexFaces(indexFacesRequest);
    }

    private static String getFaceCollection(AmazonRekognition amazonRekognition) {
        //2. Create a face collection
        String collectionId = COLLECTION_ID;
        System.out.println("Creating collection: " + collectionId );
        CreateCollectionRequest request = new CreateCollectionRequest().withCollectionId(collectionId);
        CreateCollectionResult createCollectionResult = amazonRekognition.createCollection(request);
        return collectionId;
    }

    private static AmazonRekognition getAmazonRekognitionCredentials() {
        //Create the credentials
        AWSCredentialsProvider credentialsProvider = new ProfileCredentialsProvider(PROFILE_NAME);
        AWSCredentials credentials = credentialsProvider.getCredentials();

        //Create the API client
        Regions region = Regions.US_WEST_2;

        return AmazonRekognitionClientBuilder
                .standard()
                .withRegion(region)
                .withCredentials(new AWSStaticCredentialsProvider(credentials))
                .build();
    }
}