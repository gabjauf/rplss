import { Component, OnInit } from '@angular/core';
import { SocketService } from '../socket.service';
import { Validators, FormBuilder } from '@angular/forms';

@Component({
  selector: 'app-auth',
  templateUrl: './auth.component.html',
  styleUrls: ['./auth.component.scss']
})
export class AuthComponent implements OnInit {

  form = this._fb.group({
    login: ['', Validators.required]
  });

  constructor(private _socketService: SocketService, private _fb: FormBuilder) { }

  ngOnInit() {
    this._socketService.onMessage.subscribe((message) => {
      console.log(message);
    });
  }

  submitForm() {
    this._socketService.auth(this.form.value.login);
  }

}
