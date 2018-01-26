import {Component, OnInit, ViewChild} from '@angular/core';

import { VideoFormService } from "./video-form.service";

@Component({
  selector: 'app-video-form',
  providers: [VideoFormService],
  templateUrl: './video-form.component.html',
  styleUrls: ['./video-form.component.css']
})
export class VideoFormComponent implements OnInit {

  uploadService : VideoFormService;

  constructor(videoFormService : VideoFormService) {
    this.uploadService = videoFormService;
  }

  ngOnInit() {
  }

  @ViewChild("fileInput") fileInput;

  addFile(): void {
    let fi = this.fileInput.nativeElement;
    if (fi.files && fi.files[0]) {
      let fileToUpload = fi.files[0];
      this.uploadService
        .upload(fileToUpload)
        .subscribe(res => {
          console.log(res);
        });
    }
  }

  @ViewChild('videoPlayer') videoplayer: any;

  toggleVideo(event: any) {
    this.videoplayer.nativeElement.play();
  }

}
