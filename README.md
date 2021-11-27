# TicTacToe-over-TCP
A TicTacToe game played by two players on a terminal over a TCP connection either on the same computer, on the same network or over the internet.
Each player runs the game in their terminal and before-hand must've decided on which one will act host and which one will connect to host.
Host uses either 0 or 1 parameter when running while the second player uses 2 parameters to connect. 
Threads are used to send messages and moves between the two players with safeguards preventing players from acting when it is not their turn. 
No graphical interface is used but the techniques here can easily be applied with UI for a better implementation. 
