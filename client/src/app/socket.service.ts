import { Injectable } from '@angular/core';
import * as socketIo from 'socket.io-client';
import { Observable } from 'rxjs';

const SERVER_URL = 'ws://127.0.0.1:4242';

@Injectable({
  providedIn: 'root'
})
export class SocketService {

  private socket;

    public initSocket(): void {
        // this.socket = socketIo(SERVER_URL);
        this.socket = new WebSocket(SERVER_URL);
        this.socket.onopen = (ev) => {
          console.log('Connection opened.', ev);
        };
        this.socket.onmessage = (ev) => {
          console.log('Response from server: ' + ev.data);
        };
        this.socket.onclose = (ev) => {
          console.log('Connection closed.', ev);
        };
        this.socket.onerror = (ev) => {
          console.log('An error occurred. Sorry for that.', ev);
        };
    }

    public onMessage(): Observable<any> {
        return new Observable<any>(observer => {
            this.socket.on('message', (data: any) => observer.next(data));
        });
    }

    public onEvent(event: Event): Observable<any> {
        return new Observable<any>(observer => {
            this.socket.on(event, () => observer.next());
        });
    }

    public authReq() {
      return this.socket.send('AUTH_REQ--------\n');
    }

    public auth(login) {
      return this.socket.send('AUTH------------' + login + '\n');
    }
}
