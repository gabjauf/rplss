import { Component, OnInit } from '@angular/core';
import { SocketService } from '../socket.service';
import { Observable } from 'rxjs';

@Component({
  selector: 'app-lobby',
  templateUrl: './lobby.component.html',
  styleUrls: ['./lobby.component.scss']
})
export class LobbyComponent implements OnInit {

  lobby: Observable<any>;

  constructor(private _socketService: SocketService) { }

  ngOnInit() {
    this.lobby = this._socketService.lobby;
  }

}
