import { Component } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { AppService } from './app.service';
import { Router } from '@angular/router';

import { finalize } from 'rxjs/operators';

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

  constructor(private http: HttpClient, private app: AppService, private router: Router) {
    //this.app.authenticate(undefined, undefined);
  }

  logout() {
    this.http.post('logout', {}).pipe(
      finalize(() => {
        this.app.authenticated = false;
        this.router.navigateByUrl('/login');
      })
    ).subscribe();
  }

}
