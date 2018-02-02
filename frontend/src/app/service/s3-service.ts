import { Injectable, EventEmitter } from '@angular/core';
import { HttpClient, HttpErrorResponse } from '@angular/common/http';
import { Observable } from 'rxjs/Observable';
import 'rxjs/add/observable/of';
import 'rxjs/add/operator/catch';
import 'rxjs/add/operator/map';
import * as AWS from 'aws-sdk';
import { AWS_ENV } from '../../environments/environment';

@Injectable()
export class S3Service
{
  private s3: AWS.S3;
  public progress: EventEmitter<AWS.S3.ManagedUpload.Progress> = new EventEmitter<AWS.S3.ManagedUpload.Progress>();


  constructor(private http: HttpClient) { }


  /**
   * @desc set CognitoIdentityId
   *
   * @return string IdentityId: ex) ap-northeast-1:01234567-9abc-df01-2345-6789abcd
   */
  public initialize(): Observable<boolean>
  {
    return this.http.get('/assets/cognito.php')
      .map((res: any) => {
        // res
        // ...

        // Amazon Cognito
        AWS.config.region = AWS_ENV.region;
        AWS.config.credentials = new AWS.CognitoIdentityCredentials({
          IdentityId: res.results[0].IdentityId,
        });
        this.s3 = new AWS.S3({
          apiVersion: AWS_ENV.s3.apiVersion,
          params: {Bucket: AWS_ENV.s3.Bucket},
        });
        return true;
      })

      .catch((err: HttpErrorResponse) => {
        console.error(err);
        return Observable.of(false);
      });
  }

  /**
   * @desc AWS.S3
   */
  public onManagedUpload(file: File): Promise<AWS.S3.ManagedUpload.SendData>
  {
    let params: AWS.S3.Types.PutObjectRequest = {
      Bucket: AWS_ENV.s3.Bucket,
      Key: file.name,
      Body: file,
    };
    let options: AWS.S3.ManagedUpload.ManagedUploadOptions = {
      params: params,
      partSize: 64*1024*1024,
    };
    let handler: AWS.S3.ManagedUpload = new AWS.S3.ManagedUpload(options);
    handler.on('httpUploadProgress', this._httpUploadProgress.bind(this));
    handler.send(this._send.bind(this));
    return handler.promise();
  }

  /**
   * @desc progress
   */
  private _httpUploadProgress(progress: AWS.S3.ManagedUpload.Progress): void
  {
    this.progress.emit(progress);
  }

  /**
   * @desc send
   */
  private _send(err: AWS.AWSError, data: AWS.S3.ManagedUpload.SendData): void
  {
    console.log('send()', err, data);
  }
}
