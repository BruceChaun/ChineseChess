# Chinese Chess

* Use Swing for the graphical user interface.

* Extract features like which side to move, number of each type of pieces, presence and coordinates of each piece, attacker and defender maps of each piece and each position.

* Positive samples are the moves of real Chinese chess players and negative ones are those they do not choose to move.

* The naive model is to use neural nets to train the positive and negative moves and get the fitness score.

* More advanced method is, e.g. temporal difference learning.

* After training, we use search tree to find the optimal move.
