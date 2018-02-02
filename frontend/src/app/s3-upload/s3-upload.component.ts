import { Component, OnInit } from '@angular/core';
import { S3Service } from '../service/s3-service';
import * as AWS from 'aws-sdk';

@Component({
  selector: 'app-s3-upload',
  templateUrl: './s3-upload.component.html',
  styleUrls: ['./s3-upload.component.css']
})
export class S3UploadComponent implements OnInit {

  public httpUploadProgress: {[name: string]: any} = {
    ratio : 0,
    style : {
      width: '0',
    }
  };


  /**
   * @desc constructor
   */
  constructor(private s3Service: S3Service)
  {
    this.s3Service.initialize()
      .subscribe((res: boolean) => {
        if (! res) {
          console.log('S3 cognito init error');
        }
      })
  }


  /**
   * @desc Angular LifeCycle
   */
  ngOnInit()
  {
    this.s3Service.progress
      .subscribe((res: AWS.S3.ManagedUpload.Progress) => {
        this.httpUploadProgress.ratio = res.loaded * 100 / res.total;
        this.httpUploadProgress.style.width = this.httpUploadProgress.ratio + '%';
      });
  }


  /**
   * @desc file upload
   */
  public upload(event: Event): void
  {
    this.httpUploadProgress.ratio = 0;
    this.httpUploadProgress.style.width = '0';
    this.s3Service.onManagedUpload((<HTMLInputElement>event.target).files[0]);
  }


}
