import { Component, OnInit } from '@angular/core';
import { AuthService } from '../auth.service';
import { HttpClient, HttpHeaders } from '@angular/common/http';

interface T {
  id: string;
  content: string;
}

@Component({
  selector: 'app-home',
  templateUrl: './home.component.html',
  styleUrls: ['./home.component.css']
})
export class HomeComponent implements OnInit {

  title = 'Demo';
  greeting:T = { id: '', content: ''};

  constructor(private app: AuthService, private http: HttpClient) {
      http.get('token').subscribe(data => {
        const token = data['token'];
        http.get('http://localhost:9000/api/resource', {headers : new HttpHeaders().set('X-Auth-Token', token)})
          .subscribe((data:T) => this.greeting = data)
      });
  }

  ngOnInit(): void {
  }

  authenticated() { return this.app.authenticated; }

}
