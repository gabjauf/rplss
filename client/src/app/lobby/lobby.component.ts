import { Component, OnInit, OnDestroy } from '@angular/core';
import { SocketService } from '../socket.service';
import { Observable, Subscription } from 'rxjs';
import { Router } from '@angular/router';

@Component({
  selector: 'app-lobby',
  templateUrl: './lobby.component.html',
  styleUrls: ['./lobby.component.scss']
})
export class LobbyComponent implements OnInit, OnDestroy {

  lobby: Observable<any>;

  report$ = this._socketService.report;
  gameOverMessage = this._socketService.gameMessage;

  subscriptions = new Subscription();

  constructor(private _socketService: SocketService, private _router: Router) { }

  ngOnInit() {
    this.lobby = this._socketService.lobby;
    this.subscriptions.add(this._socketService.onMessage.subscribe((message) => {
      console.log(message);
      switch (message.command) {
        case 'AUTH_REQ':
          this._router.navigate(['/game']);
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

}
