# rplss

Multiplayer Rock Paper Lizard Scissor Spock in Java / Javascript made with websockets.

# Features

- Handle multiple games at once (though I did not make a benchmark)
- Chat in the lobby
- You can challenge the other players!

# Setup

## Server side
This project is built with java 9.
I used maven for packages (IntelliJ should download automatically).
Use IntelliJ to launch.

You can configure the following:
- RPSLS_PORT: port number (default 4242, need to synchronize with front-end because angular does not support setting from env)
- RPSLS_HOST: host name (default: 127.0.0.1)
- RPSLS_MOVE_TIMEOUT: for move timeout (should also be configured in front-end environment.ts)
- RPSLS_LOGIN_TIMEOUT: defines how often the server will ask the client for credentials

## Front-end side
Angular projet (v7)
```cd client && npm i```
then ```npm start```
Should launch the front end on port 4200.

# TODO

- Ranking system
- Admin page to track what is going on

