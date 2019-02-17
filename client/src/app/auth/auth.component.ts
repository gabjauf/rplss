import { Component, OnInit } from '@angular/core';
import { SocketService } from '../socket.service';
import { Validators, FormBuilder } from '@angular/forms';
import { Router } from '@angular/router';

@Component({
  selector: 'app-auth',
  templateUrl: './auth.component.html',
  styleUrls: ['./auth.component.scss']
})
export class AuthComponent implements OnInit {

  form = this._fb.group({
    login: ['', Validators.required]
  });

  login = '';
  error = '';

  constructor(private _socketService: SocketService, private _fb: FormBuilder, private _router: Router) { }

  ngOnInit() {
    this._socketService.onMessage.subscribe((message) => {
      switch (message.command) {
        case 'LOBBY_JOINED':
          if (message.parameters === this.login) {
            this._router.navigate(['/lobby']);
          }
          break;
        case 'LOBBY':
          this.error = 'Username is already in use';
      }
    });
  }

  submitForm() {
    this.error = '';
    this.login = this.form.value.login;
    this._socketService.auth(this.form.value.login);
  }

}
