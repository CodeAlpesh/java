import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';

@Injectable({
  providedIn: 'root'
})
export class AuthService {

  authenticated = false;

  constructor(private http: HttpClient) {
  }

  authenticate(credentials, callback) {

    // const headers = new HttpHeaders(credentials ? {
    //   authorization: 'Basic ' + btoa(credentials.username + ':' + credentials.password)
    // } : {});

    let body: HttpParams = new HttpParams();
    body = body.append('username', credentials.username);
    body = body.append('password', credentials.password);

    this.http.post('/api/login', body).subscribe(
      response => {
        console.log(response['success'] + '----' + response);
        if (response['success']) {
          this.authenticated = true;
      } else {
          this.authenticated = false;
      }
      return callback && callback();
      },
      error => {
        console.log("error");
      },
      () => {
        console.log("completed");
      }
    );

   }
}
