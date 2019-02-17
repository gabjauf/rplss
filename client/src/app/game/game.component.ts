import { Component, OnInit } from '@angular/core';
import { SocketService } from '../socket.service';
import { Router } from '@angular/router';
import { Observable, interval } from 'rxjs';
import { take, map, startWith } from 'rxjs/operators';

@Component({
  selector: 'app-game',
  templateUrl: './game.component.html',
  styleUrls: ['./game.component.scss']
})
export class GameComponent implements OnInit {
  moves = ['rock', 'paper', 'scissor', 'lizard', 'spock'];
  timer: Observable<any>;
  login: string;

  constructor(private _socketService: SocketService, private _router: Router) {}

  ngOnInit() {
    this.login = this._socketService.login;
    this._socketService.onMessage.subscribe((message) => {
      switch (message.command) {
        case 'MOVE_REQ':
          this.resetTimer();
          break;
      }
    });
  }

  resetTimer() {
    const start = 10;
    this.timer = interval(1000)
    .pipe(take(start), map(i => start - i - 1), startWith(start));
  }

  sendGesture(gesture: string) {
    this._socketService.sendGesture(gesture);
  }
}
