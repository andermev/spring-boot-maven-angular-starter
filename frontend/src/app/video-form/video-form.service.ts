import { Injectable } from '@angular/core';
import { HttpClient } from "@angular/common/http";

@Injectable()
export class VideoFormService {

  constructor(private http: HttpClient) {}

  upload(fileToUpload: any) {
    let input = new FormData();
    input.append("file", fileToUpload);

    return this.http.post("/api/uploadFile", input);
  }
}
