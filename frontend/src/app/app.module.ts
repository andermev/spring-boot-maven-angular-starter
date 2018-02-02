import { HttpClientModule } from '@angular/common/http';
import { BrowserModule } from '@angular/platform-browser';
import { NgModule } from '@angular/core';
import { FormsModule }   from '@angular/forms';

import { AppComponent } from './app.component';
import { VideoFormComponent } from './video-form/video-form.component';
import { S3DownloadComponent } from './s3-download/s3-download.component';
import { S3Service } from './service/s3-service';
import { S3UploadComponent } from './s3-upload/s3-upload.component';

@NgModule({
  declarations: [
    AppComponent,
    VideoFormComponent,
    S3DownloadComponent,
    S3UploadComponent
  ],
  imports: [
    BrowserModule,
    FormsModule,
    HttpClientModule
  ],
  providers: [S3Service],
  bootstrap: [AppComponent]
})
export class AppModule { }
