import { Component, OnInit } from '@angular/core';
import { AuthService } from '../auth.service';
import { HttpClient } from '@angular/common/http';

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
    http.get('http://localhost:9000/api/resource').subscribe((data:T) => this.greeting = data);
  }

  ngOnInit(): void {
  }

  authenticated() { return this.app.authenticated; }

}
