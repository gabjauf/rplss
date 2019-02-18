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
  status: any = {
    winner: '',
    playerScore: 0,
    opponentScore: 0
  };

  constructor(private _socketService: SocketService, private _router: Router) {}

  ngOnInit() {
    this.login = this._socketService.login;
    this._socketService.onMessage.subscribe((message) => {
      switch (message.command) {
        case 'MOVE_REQ':
          this.resetTimer();
          break;
        case 'STATUS':
          this.setStatus(message.parameters);
          break;

      }
    });
  }

  resetTimer() {
    const start = 10;
    this.timer = interval(1000)
    .pipe(take(start), map(i => start - i - 1), startWith(start));
  }

  setStatus(status) {
    const [winner, winnerScore, loserScore] = status.split(';');
    if (!winner) {
      return;
    } else if (winner === this.login) {
      this.status = {
        winner,
        playerScore: parseInt(winnerScore),
        opponentScore: parseInt(loserScore),
      };
    } else {
      this.status = {
        winner,
        playerScore: parseInt(loserScore),
        opponentScore: parseInt(winnerScore),
      };
    }
  }

  sendGesture(gesture: string) {
    this._socketService.sendGesture(gesture);
  }
}
