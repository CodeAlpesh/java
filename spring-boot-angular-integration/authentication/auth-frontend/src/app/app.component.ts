import { Component } from '@angular/core';
import { HttpClient } from '@angular/common/http';

@Component({
  selector: 'app-root',
  templateUrl: './app.component.html',
  styleUrls: ['./app.component.css']
})
export class AppComponent {
  title = 'Authentication App';

  greeting: any = {
    id: '',
    content: ''
  };

  constructor(private http: HttpClient) {
    this.http.get('/api/resource').subscribe(data => this.greeting = data);
  }

}
