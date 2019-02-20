import { Injectable } from '@angular/core';
import { Observable, Subject, ReplaySubject } from 'rxjs';
import { Router } from '@angular/router';
import { environment } from 'src/environments/environment';

const SERVER_URL = `ws://${environment.RPSLS_HOST}:${environment.RPSLS_PORT}`;

@Injectable({
  providedIn: 'root'
})
export class SocketService {
  private socket;
  public onMessage = new Subject<any>();
  public lobby = new ReplaySubject<any>(1);
  public gameMessage = new ReplaySubject<any>(1);
  public report = new ReplaySubject<any>(1);
  public login: string;

  constructor(private _router: Router) {
    if (!this.login) {
      this._router.navigate(['auth']);
    }
  }

  public initSocket(): void {
    this.socket = new WebSocket(SERVER_URL);
    this.socket.onopen = ev => {
      console.log('Connection opened.', ev);
    };
    this.socket.onmessage = ev => {
      console.log('Response from server: ' + ev.data);
      const parsedMessage = this.parseMessage(ev.data);
      switch (parsedMessage.command) {
        case 'LOBBY':
          this.lobby.next(parsedMessage.parameters.split('_'));
          break;
      }
      this.onMessage.next(parsedMessage);
    };
    this.socket.onclose = ev => {
      console.log('Connection closed.', ev);
    };
    this.socket.onerror = ev => {
      console.log('An error occurred. Sorry for that.', ev);
    };
  }

  public auth(login: string) {
    return this.socket.send(`AUTH------------${login}\n`);
  }

  public sendGesture(gesture: string) {
    return this.socket.send(`MOVE------------${gesture}\n`);
  }

  public sendChatMessage(message: string) {
    return this.socket.send(`CHAT------------${this.login};${message}\n`);
  }

  public sendChallenge(challengedPlayer) {
    return this.socket.send(`CHALLENGE-------${this.login};${challengedPlayer}\n`);
  }

  public replyChallengeKO(challenger) {
    return this.socket.send(`CHALLENGE_KO----${challenger};${this.login}\n`);
  }

  public replyChallengeOK(challenger) {
    return this.socket.send(`CHALLENGE_OK----${challenger};${this.login}\n`);
  }

  parseMessage(message: string) {
    return {
      command: message.substring(0, 15).substring(0, message.indexOf('-')),
      parameters: message.substring(16, message.indexOf('\n'))
    };
  }
}
