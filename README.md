# Code-Kata-Resk
A multiplayer, bot-controlled game of Risk. To run this game locally, ensure that you have git and gradle installed on your machine. After cloning this repository type `./gradlew bootRun` to host this locally. If you do not have permission to use `./gradlew`, use `sudo sh ./gradlew bootRun` instead.

## Game Description
Code-Kata-Resk is a four-player game based on the board game Risk. 
The goal is simple: to own as much territory as possible when the game ends after 256 turns. 
Players move in turns. On their turn, every player can commit a certain number of troops to their own territory, move troops around in their own territory, or attempt to conquer unowned territory or territory owned by other players. 
We've also spiced up the game a little by adding cards: players earn card cash by expanding into new territories, and can expend card cash to do one of three things:
 * Connect two owned tiles so that troops can move between them in one turn;
 * "Create an insurgency": place 10 unowned troops on an unowned tile, or attack an owned tile with 10 owned troops;
 * Remove all connections to an owned tile, essentially removing it from the game.

The game is played on a grid of size 25x25 where each player starts in one of the corners.
Each tile is connected at first to all tiles directly adjacent to it (not diagonals), but can be connected to other tiles with the connect card.
In order for each side to control their empire, they must call the API methods described below. 
For the API, each team will make a team password or a key during the competition.
A team wins by having the most territories at the end of the game.

### Board State

Each territory on the board is indexed by number, starting at zero in the upper-left-hand corner and going right. This means that the `n`th row starts with the index `25n`.

## Graphics

To see a graphical representation of the main competition, go to `http://localhost:8080` in your browser. 
To see a live version of a test environment, go to `http://localhost:8080/test/?teamPassword=TEAM_KEY` to see the test environment of the team of the given key.

Be aware that the graphical environment is purely for entertainment reasons and shouldn't be used as an objective grasp as to what is going on: the graphical environment is EXTREMELY slow to update and takes time to reflect changes.

## Actions 

The `URL` in the following examples is `http://localhost:8080`.
 
### Information
 
You can access information about the board state and the game by using the following API calls. API calls return `null` if they are invalid. Otherwise, API calls return either the information requested or whether a performed action actually succeeded. Information is either returned as a single primitive value ("hello world", 3, false) or in valid JSON format.

```GET URL/api/teams/order```

This call will return the turn order as a list of team colors; for instance, the output `["YELLOW", "GREEN", "BLUE", "RED"]` means that the `YELLOW` team moves first, followed in this order by the other three.
 
```GET URL/api/teams/territories - params(teamColor: String)```

This call will return a list of territories owned by the given color (`YELLOW`, `BLUE`, `RED`, or `GREEN`) as a list of location IDs. 
A use of this call might look like this: `GET URL/api/teams/territories?teamColor=BLUE`.

```GET URL/api/board/size```

This call will return the total number of territories in the game.

```GET URL/api/board/adjacencies - params(id: Int)```

This call will return a list of the IDs for all tiles that are connected to the tile with given ID.

```GET URL/api/board/troops - params(id: Int)```

This call will return a JSON object containing the owner of the territory with given ID and the number of troops in that territory.
Unowned territories have `owner: null` if they have troops, but will only return `null` if they are unoccupied. For example, an output might look like this: `{owner:YELLOW, amount:2}`.

```GET URL/api/actions```

This call will return an ordered list of every action taken in the form 'team action inputs'
Actions are (brackets represent variable values, and won't show up in output.)
 * 	`commit <location> <amount>` represents the addition of `amount` troops to `location`.
 * 	`move <from> <to> <amount>` represents the movement of `amount` troops from `from` to `to`.
 * 	`connect <tile1> <tile2>` represents the usage of a card to create a connection between `tile1` and `tile2`.
 * 	`insurgency <tile>` represents the usage of a card to either add 10 troops to `tile` if it is unowned or attack `tile` with 10 unowned troops otherwise.
 * 	`disconnect <tile>` represents the usage of a card to remove all connections from `tile`.
 * 	`end` represents the end of a player's turn.
The latest action is at the end. An ideal way to use the log is to parse which tiles have activity going on in them, and then query those tiles specifically to find out more accurate information.

```GET URL/api/cards/amount - params(teamColor: String)```

This call will return the amount of available card cash for the team with the given color.

```GET URL/api/troops/amount```

Gets the amount of troops the current player has yet to commit. A player's turn ends when they have committed all of their troops.

### Actions

Controlling troops and using cards is also done by API call as follows. 
Each method will return `true` or `false` depending on whether the action is successful. They can also return `null` if the input is nonsensical (tile ID out of bounds, team password is wrong, etc.)

```POST URL/api/troops/add - params(teamPassword: String, locationId: Int, amount: Int)```

Commits `amount` troops to the territory with ID `locationId`, provided you own that territory. 
An API call with multiple inputs uses ampersands (`&`) to separate them. 
For instance, a valid call for the `YELLOW` team with password `resk` is 
`POST URL/api/troops/add?teamPassword=resk&locationId=0&amount=10`.
Order of inputs is not important, as long as they are all in the call.

```POST URL/api/troops/move - params(teamPassword: String, fromId: Int, toId: Int, amount: Int)```

Moves `amount` troops from the territory with ID `fromId` to the territory with ID `toId` provided that the two territories are connected and the territory with ID `fromId` is owned by the player with the given password.
If the `toId` territory is owned by another player, this becomes an attack. 
The attacking player loses a number of troops equal to half of the size of the defending force (rounded down), and the defending player loses a number of troops equal to 3/4 of the size of the attacking force (rounded down).
If all defending troops are killed and there are attacking troops remaining, the remaining troops move into the territory and capture it for the attacking side.

```PUT URL/api/cards/connect - params(teamPassword: String, tileId1: Int, tileId2: Int)```

Expends 1 card cash to create a connection between `tileId1` and `tileId2` provided the player owns both tiles.

```POST URL/api/cards/inspireInsurgency - params(teamPassword: String, tileId: Int)```

Expends 4 card cash to place 10 unowned troops on the territory with ID `tileId`. 
If the territory is owned, the unowned troops will instead attack the territory (in other words, the territory loses 7 troops, and if it becomes unowned any unowned troops remain.)

```PUT URL/api/cards/disconnect - params(teamPassword: String, tileId: Int)```

Expends 4 card cash to remove all connections to the tile with ID `tileId`.

## Test Environment

Each team can access a test environment where they are the only player. 
The test environment can be accessed using any of the above API calls with `URL/api` as `URL/test/api`.
Any API call in a test environment needs to have `teamPassword` as a parameter, even if it doesn't do so in the documentation.
A team can also reset its test environment using the call
```POST URL/test/api - params(teamPassword: String)```.

If any issues are found, please let either Saurabh or Elia know so they can fix it. Have fun!
