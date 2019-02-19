import { Component, OnInit } from '@angular/core';
import { SocketService } from '../socket.service';
import { Router } from '@angular/router';
import { Observable, interval } from 'rxjs';
import { take, map, startWith } from 'rxjs/operators';
import { environment } from 'src/environments/environment';

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
  gameOver = false;
  report = '';
  gameOverMessage = '';
  MOVE_TIMEOUT = environment.MOVE_TIMEOUT;

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
        case 'REPORT':
          this.report = message.parameters;
          break;
        case 'WIN':
        case 'LOSE':
          this.onGameEnd(message.parameters);
          break;
      }
    });
  }

  downloadReport() {
    const blob = new Blob([this.report], { type: 'text/xml' });
    const url = window.URL.createObjectURL(blob);
    window.open(url);
  }

  resetTimer() {
    const start = this.MOVE_TIMEOUT;
    this.timer = interval(1000)
    .pipe(take(start), map(i => start - i - 1), startWith(start));
  }

  onGameEnd(parameters) {
    this.gameOver = true;
    this.gameOverMessage = this.parseEndGameMessage(parameters);
  }

  parseEndGameMessage(parameters) {
    const [winner, loser, loserScore] = parameters.split(';');
    return winner === this.login ?
      `you won 8 to ${loserScore} against ${loser}`
      : `you lost 8 to ${loserScore} against ${winner}`;
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
