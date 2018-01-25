package com.qwerty.app.videos;

import com.amazonaws.AmazonClientException;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.rekognition.AmazonRekognition;
import com.amazonaws.services.rekognition.AmazonRekognitionClientBuilder;
import com.amazonaws.services.rekognition.model.*;
import com.amazonaws.services.sns.AmazonSNS;
import com.amazonaws.services.sns.AmazonSNSClientBuilder;
import com.amazonaws.services.sqs.AmazonSQS;
import com.amazonaws.services.sqs.AmazonSQSClientBuilder;
import com.amazonaws.services.sqs.model.Message;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.List;

public class VideoDetect {
  private static AmazonSNS sns = null;
  private static AmazonSQS sqs = null;
  private static AmazonRekognition rek = null;
  private static NotificationChannel channel= new NotificationChannel()
          .withSNSTopicArn("")
          .withRoleArn("");

  private static String queueUrl =  "";
  private static String startJobId = null;

  private static final String ACCESS_KEY = "";
  private static final String SECRET_KEY = "";

  public static void main(String[] args)  throws Exception{


    AWSStaticCredentialsProvider credentials;

    try {
      credentials = new AWSStaticCredentialsProvider(new BasicAWSCredentials(ACCESS_KEY, SECRET_KEY));
    } catch (Exception e) {
      throw new AmazonClientException("Cannot load the credentials from the credential profiles file. "
              + "Please make sure that your credentials file is at the correct "
              + "location (/Users/userid>.aws/credentials), and is in valid format.", e);
    }



    sns = AmazonSNSClientBuilder
            .standard()
            .withRegion(Regions.US_EAST_1)
            .withCredentials(credentials)
            .build();

    sqs = AmazonSQSClientBuilder
            .standard()
            .withRegion(Regions.US_EAST_1)
            .withCredentials(credentials)
            .build();


    rek = AmazonRekognitionClientBuilder.standard()
            .withCredentials(credentials)
            .withRegion(Regions.US_EAST_1)
            .build();

    String collectionId = "exampleCollection";
    System.out.println("Creating collection: " +
            collectionId );

    CreateCollectionRequest request = new CreateCollectionRequest()
            .withCollectionId(collectionId);
    //rek.createCollection(request);

    Image image=new Image()
            .withS3Object(new S3Object()
                    .withBucket("videoqwe")
                    .withName("image.png"));

    IndexFacesRequest indexFacesRequest = new IndexFacesRequest()
            .withImage(image)
            .withCollectionId(collectionId)
            .withExternalImageId("image.png")
            .withDetectionAttributes("ALL");

    //rek.indexFaces(indexFacesRequest);

    //=================================================
    StartFaceSearchCollection("videoqwe", "video.avi");
    //=================================================
    System.out.println("Waiting for job: " + startJobId);
    //Poll queue for messages
    List<Message> messages=null;
    int dotLine=0;
    boolean jobFound=false;

    //loop until the job status is published. Ignore other messages in queue.
    do{
      //Get messages.
      do{
        messages = sqs.receiveMessage(queueUrl).getMessages();
        if (dotLine++<20){
          System.out.print(".");
        }else{
          System.out.println();
          dotLine=0;
        }
      }while(messages.isEmpty());

      System.out.println();

      //Loop through messages received.
      for (Message message: messages) {
        String notification = message.getBody();

        // Get status and job id from notification.
        ObjectMapper mapper = new ObjectMapper();
        JsonNode jsonMessageTree = mapper.readTree(notification);
        JsonNode messageBodyText = jsonMessageTree.get("Message");
        ObjectMapper operationResultMapper = new ObjectMapper();
        JsonNode jsonResultTree = operationResultMapper.readTree(messageBodyText.textValue());
        JsonNode operationJobId = jsonResultTree.get("JobId");
        JsonNode operationStatus = jsonResultTree.get("Status");
        System.out.println("Job found was " + operationJobId);
        // Found job. Get the results and display.
        if(operationJobId.asText().equals(startJobId)){
          jobFound=true;
          System.out.println("Job id: " + operationJobId );
          System.out.println("Status : " + operationStatus.toString());
          if (operationStatus.asText().equals("SUCCEEDED")){
            //============================================
            GetResultsFaceSearchCollection();
            //============================================
          }
          else{
            System.out.println("Video analysis failed");
          }

          sqs.deleteMessage(queueUrl,message.getReceiptHandle());
        }

        else{
          System.out.println("Job received was not job " +  startJobId);
        }
      }
    } while (!jobFound);


    System.out.println("Done!");
  }


  //Faces=======================================================================

  private static void StartFaces(String bucket, String video) throws Exception{

    StartFaceDetectionRequest req = new StartFaceDetectionRequest()
            .withVideo(new Video()
                    .withS3Object(new S3Object()
                            .withBucket(bucket)
                            .withName(video)))
            .withNotificationChannel(channel);



    StartFaceDetectionResult startLabelDetectionResult = rek.startFaceDetection(req);
    startJobId=startLabelDetectionResult.getJobId();

  }

  private static void GetResultsFaces() throws Exception{

    int maxResults=10;
    String paginationToken=null;
    GetFaceDetectionResult faceDetectionResult=null;

    do{
      if (faceDetectionResult !=null){
        paginationToken = faceDetectionResult.getNextToken();
      }

      faceDetectionResult = rek.getFaceDetection(new GetFaceDetectionRequest()
              .withJobId(startJobId)
              .withNextToken(paginationToken)
              .withMaxResults(maxResults));

      VideoMetadata videoMetaData=faceDetectionResult.getVideoMetadata();

      System.out.println("Format: " + videoMetaData.getFormat());
      System.out.println("Codec: " + videoMetaData.getCodec());
      System.out.println("Duration: " + videoMetaData.getDurationMillis());
      System.out.println("FrameRate: " + videoMetaData.getFrameRate());


      //Show faces, confidence and detection times
      List<FaceDetection> faces= faceDetectionResult.getFaces();

      for (FaceDetection face: faces) {
        long seconds=face.getTimestamp()/1000;
        System.out.print("Sec: " + Long.toString(seconds) + " ");
        System.out.println(face.getFace().toString());
        System.out.println();
      }
    } while (faceDetectionResult !=null && faceDetectionResult.getNextToken() != null);


  }

  private static void StartLabels(String bucket, String video) throws Exception{

    StartLabelDetectionRequest req = new StartLabelDetectionRequest()
            .withVideo(new Video()
                    .withS3Object(new S3Object()
                            .withBucket(bucket)
                            .withName(video)))
            .withMinConfidence(50F)
            .withJobTag("DetectingLabels")
            .withNotificationChannel(channel);

    StartLabelDetectionResult startLabelDetectionResult = rek.startLabelDetection(req);
    startJobId=startLabelDetectionResult.getJobId();

  }

  private static void GetResultsLabels() throws Exception{

    int maxResults=10;
    String paginationToken=null;
    GetLabelDetectionResult labelDetectionResult=null;

    do {
      if (labelDetectionResult !=null){
        paginationToken = labelDetectionResult.getNextToken();
      }

      GetLabelDetectionRequest labelDetectionRequest= new GetLabelDetectionRequest()
              .withJobId(startJobId)
              .withSortBy(LabelDetectionSortBy.TIMESTAMP)
              .withMaxResults(maxResults)
              .withNextToken(paginationToken);


      labelDetectionResult = rek.getLabelDetection(labelDetectionRequest);

      VideoMetadata videoMetaData=labelDetectionResult.getVideoMetadata();

      System.out.println("Format: " + videoMetaData.getFormat());
      System.out.println("Codec: " + videoMetaData.getCodec());
      System.out.println("Duration: " + videoMetaData.getDurationMillis());
      System.out.println("FrameRate: " + videoMetaData.getFrameRate());


      //Show labels, confidence and detection times
      List<LabelDetection> detectedLabels= labelDetectionResult.getLabels();

      for (LabelDetection detectedLabel: detectedLabels) {
        long seconds=detectedLabel.getTimestamp()/1000;
        System.out.print("Sec: " + Long.toString(seconds) + " ");
        System.out.println("\t" + detectedLabel.getLabel().getName() +
                "     \t" +
                detectedLabel.getLabel().getConfidence().toString());
        System.out.println();
      }
    } while (labelDetectionResult !=null && labelDetectionResult.getNextToken() != null);

  }

  //Face collection search in video ==================================================================
  private static void StartFaceSearchCollection(String bucket, String video) throws Exception{


    StartFaceSearchRequest req = new StartFaceSearchRequest()
            .withCollectionId("exampleCollection")
            .withVideo(new Video()
                    .withS3Object(new S3Object()
                            .withBucket(bucket)
                            .withName(video)))
            .withNotificationChannel(channel);



    StartFaceSearchResult startPersonCollectionSearchResult = rek.startFaceSearch(req);
    startJobId=startPersonCollectionSearchResult.getJobId();

  }

  private static void GetResultsFaceSearchCollection() throws Exception{

    GetFaceSearchResult faceSearchResult=null;
    int maxResults=10;
    String paginationToken=null;

    do {

      if (faceSearchResult !=null){
        paginationToken = faceSearchResult.getNextToken();
      }


      faceSearchResult  = rek.getFaceSearch(
              new GetFaceSearchRequest()
                      .withJobId(startJobId)
                      .withMaxResults(maxResults)
                      .withNextToken(paginationToken)
                      .withSortBy(FaceSearchSortBy.TIMESTAMP)
      );


      VideoMetadata videoMetaData=faceSearchResult.getVideoMetadata();

      System.out.println("Format: " + videoMetaData.getFormat());
      System.out.println("Codec: " + videoMetaData.getCodec());
      System.out.println("Duration: " + videoMetaData.getDurationMillis());
      System.out.println("FrameRate: " + videoMetaData.getFrameRate());
      System.out.println();


      //Show search results
      List<PersonMatch> matches=
              faceSearchResult.getPersons();

      for (PersonMatch match: matches) {
        long seconds=match.getTimestamp()/1000;
        System.out.print("Sec: " + Long.toString(seconds));
        System.out.println(" Person number: " + match.getPerson().getIndex());
        List <FaceMatch> faceMatches = match.getFaceMatches();
        System.out.println("Matches in collection...");
        if (faceMatches != null) {
          for (FaceMatch faceMatch : faceMatches) {
            Face face = faceMatch.getFace();
            System.out.println("Face Id: " + face.getFaceId());
            System.out.println("Similarity: " + faceMatch.getSimilarity().toString());
            System.out.println();
          }
        }
        System.out.println();
      }

      System.out.println();

    } while (faceSearchResult !=null && faceSearchResult.getNextToken() != null);

  }
}