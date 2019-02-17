import { Component, OnInit } from '@angular/core';
import { SocketService } from '../socket.service';
import { Observable } from 'rxjs';
import { Router } from '@angular/router';

@Component({
  selector: 'app-lobby',
  templateUrl: './lobby.component.html',
  styleUrls: ['./lobby.component.scss']
})
export class LobbyComponent implements OnInit {

  lobby: Observable<any>;

  constructor(private _socketService: SocketService, private _router: Router) { }

  ngOnInit() {
    this.lobby = this._socketService.lobby;
    this._socketService.onMessage.subscribe((message) => {
      console.log(message);
      switch (message.command) {
        case 'AUTH_REQ':
          this._router.navigate(['/game']);
          break;
      }
    });
  }

}
