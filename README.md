# ultimate-tic-tac-toe-bot

This is a bot that was designed to play [ultimate tic tac toe](https://en.wikipedia.org/wiki/Ultimate_tic-tac-toe) in a competition at theaigames.com. It uses an alpha-beta search algorithm with the following improvements:

- Principal variation search
- Quiescence search
- Transposition tables
- Hash move and history heuristic for move ordering
- Null move reductions (currently disabled)
- Genetic-algorithm-tuned board evaluation features

Its strength is nothing special, but nonetheless building it was a fun exercise.

## Usage

The bot is a java program with no external dependencies. To run it in the TheAIGames competition, simply zip the contents of the `src` folder and upload it to the platform. 

It is also possible to communicate it via console using the [ultimate tic tac toe competition protocol](http://theaigames.com/competitions/ultimate-tic-tac-toe/getting-started) -- writing commands to standard in and getting responses from standard out. This can be useful for simple tests.
