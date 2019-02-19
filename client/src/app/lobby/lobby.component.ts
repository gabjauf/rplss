import { Component, OnInit, OnDestroy } from '@angular/core';
import { SocketService } from '../socket.service';
import { Observable, Subscription } from 'rxjs';
import { Router } from '@angular/router';
import { FormBuilder, Validators } from '@angular/forms';
import { filter } from 'rxjs/operators';

@Component({
  selector: 'app-lobby',
  templateUrl: './lobby.component.html',
  styleUrls: ['./lobby.component.scss']
})
export class LobbyComponent implements OnInit, OnDestroy {

  lobby: any[];

  report$ = this._socketService.report;
  gameOverMessage = this._socketService.gameMessage;

  subscriptions = new Subscription();
  form: any;
  chat: any[] = [];

  constructor(private _socketService: SocketService, private _fb: FormBuilder, private _router: Router) { }

  ngOnInit() {
    this.subscriptions.add(this._socketService.lobby.subscribe(lobby => {
      this.lobby = lobby.filter(el => el !== this._socketService.login);
    }));
    this.resetChatForm();
    this.subscriptions.add(this._socketService.onMessage.subscribe((message) => {
      console.log(message);
      switch (message.command) {
        case 'AUTH_REQ':
          this._router.navigate(['/game']);
          break;
        case 'CHAT':
          this.handleChat(message.parameters);
          break;
        case 'LOBBY_LEFT':
          this.lobby = this.lobby.filter(el => el !== message.parameters);
          break;
      }
    }));
  }

  downloadReport(report: string) {
    const blob = new Blob([report], { type: 'text/xml' });
    const url = window.URL.createObjectURL(blob);
    window.open(url);
  }

  ngOnDestroy(): void {
    this.subscriptions.unsubscribe();
  }

  resetChatForm() {
    this.form = this._fb.group({
      chat: ['', Validators.required]
    });
  }

  sendChatMessage() {
    this._socketService.sendChatMessage(this.form.value.chat);
    this.resetChatForm();
  }

  handleChat(parameters) {
    const [login, message] = parameters.split(';');
    this.chat.push({login, message});
  }

}
