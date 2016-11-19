# Ghostwriters Webapp

This is the backend component for the [Ghostwriters app](https://github.com/charlescapps/ghostwriters-app).
The API is mainly used by the Android/iOS app (written in Corona).

It is a Java servlet Rest API written in JAX-RS using the Jackson framework. 

The datastore used is Postgresql. 

This was deployed in Heroku, so there are a few heroku-isms in here, but basically it uses maven and compiles to a WAR so this could be deployed anywhere.

This was incredibly fun, and a great learning experience.
The end result was a really fun game! My friends played it somewhat seriously; friends of friends played it.
There were several hundred real human users on the Leaderboard at its peak.

Unfortunately it barely made any money in in-app payments so I shut down the servers earlier this year,
thinking it best to use our money to pay the bills. It doesn't work well on the basic heroku nodes last I checked,
because it's memory hungry for all the tries and hash maps that are computed when the server starts up. 

The backend is responsible for a number of things, such as:
* User profiles and login - default password is the user's device id, but a password can be chosen and then hash is stored with salt
* Arbitrate games - create a new game, search for games, send and receive the current state of a game, etc.
* Leaderboard queries
* AI - for single player mode, the server computes the AI's move.
** The AI uses probabilistic algorithms coupled with tries and hash maps to deliver incredibly fast decisions.
* The justification for server-side AI includes:
** Simplifies game management since moves have to be validated by the server anyway.
** Wouldn't be a good idea to have these massive tries in memory on the phone.
** It works async; user can leave the game then come back later to see the server's move.
** It's a fun project! 
** It works!
* AI is also used for "special tiles" such as the Scry tile which finds a good move for the player. 
