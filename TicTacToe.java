import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;

/**
 * The class TicTacToe is a three-in-a-row game designed for two players to play on separate consoles connecting to each other using the TCP protocol.
 * 
 *
 * @author Christian Neij
 */
public class TicTacToe{
	private String IP = "localhost";
	private int port;
	private Socket socket;
	private ServerSocket serverSocket;
	private int moves = 0;
	private char[] spaces = new char[9];
	private boolean yourTurn = false;
	private boolean X = false;
	private boolean connectedToOtherPlayer = false;
	private boolean continueGame = true;
	private boolean playerWon = false;
	private boolean opponentWon = false;
	private boolean tied = false;
	private ListeningThread listenThread;
	private SendingThread sendingThread;
	private final int[][] winningSpaces = new int[][] {{0,1,2}, {3,4,5}, {6,7,8}, {0,3,6}, {1,4,7}, {2,5,8}, {0,4,8}, {2,4,6}};
	
	/**
     * The constructor with two parameters accepts a String containing the IP-address and an int containing the port.
     * The parameters are used to connect to the first player.
     * This constructor is used by the second player wishing to connect to the first player.
     *
     * @author Christian Neij
     */
	public TicTacToe(String ip, int port) {
		controlPortAcceptance();
		this.IP = ip;
		this.port = port;
		if(!connectToServer()) {
			System.exit(0);
		}
		connectedToOtherPlayer = true;
		introduceGame();
		runGame();
	}
	/**
     * The constructor with one parameter accepts an int containing the port.
     * The parameter is used when establishing a server.
     * This constructor is used by the first player wishing to establish a server for the second player to connect to.
     *
     * @author Christian Neij
     */
	public TicTacToe(int port) {
		controlPortAcceptance();
		this.port = port;
		yourTurn = true;
		X = true;
		establishServer();
		System.out.println("Awaiting other player...");
		while(!connectedToOtherPlayer) {
			awaitOtherPlayer();
		}
		introduceGame();
		runGame();
	}
	/**
     * The constructor with zero parameters will send the default port value of 2000 to the constructor accepting one parameter. 
     * This constructor is used by the first player wishing to establish a server for the second player to connect to.
     *
     * @author Christian Neij
     */
	public TicTacToe() {
		this(2000);
	}
	/**
     * The method is used by the first player to establish a server for player two to connect to.
     *
     * @author Christian Neij
     */
	private void establishServer() {
		try {
			serverSocket = new ServerSocket(port, 4);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	/**
     * For the second player to connect to the first player. Returns true if successful. 
     *
     * @author Christian Neij
     */
	private boolean connectToServer() {
		try {
			socket = new Socket(IP, port);
			return true;
		} catch (UnknownHostException e) {
			System.out.println("Failed to connect to host, launch anew and try again.");
			return false;
		} catch (IOException e) {
			e.printStackTrace();
			return false;
		}
	}
	/**
     * Method makes sure the port is within the acceptable range between 0 and 65535. IllegalArgumentException is thrown if the port number is unacceptable.
     *
     * @author Christian Neij
     */
	private void controlPortAcceptance() {
		if(this.port < 0 || port > 65535) {
			throw new IllegalArgumentException("Port number unacceptable, must be between 0 and 65535");
		}
	}
	/**
     * Method is used by the first player to check if a second player is trying to connect. 
     *
     * @author Christian Neij
     */
	private void awaitOtherPlayer() {
		socket = null;
		try {
			socket = serverSocket.accept();
			connectedToOtherPlayer = true;
			System.out.println("Other player has connected.");
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	/**
     * Information about how the game works provided to the players before the game begins. 
     *
     * @author Christian Neij
     */
	private void introduceGame() {
		System.out.println("Both players are connected and the game can begind!");
		System.out.println("The game will be played on a 3x3 board, you will submit your choice of placement by typing the placement number ranging from 0 to 8.");
		System.out.println("The numbers are placed according to this grid:");
		System.out.println("0 | 1 | 2\n--------------\n3 | 4 | 5\n---------------\n6 | 7 | 8");
		System.out.println("To place your mark write the number and only the number where you wish to place it.\nTo chat with your opponent write a message starting with 'M: '");
		if(X) {
			System.out.println("You will start, your symbol is X");
		}else {
			System.out.println("Your opponent will start, your symbol is O");
		}
	}
	/**
     * Method takes a String as a parameter, the string is a received message from the other player. 
     * If the message starts with "M:" it will be printed as a message, allowing communication between the players. 
     * Otherwise it will be assumed to be a move and will be converted to an int and the move will either be played or ignored if the space is already filled.
     *
     * @author Christian Neij
     */
	public void inputFromOtherUser(String input) {
		if(input.startsWith("M:")) {
			input = input.substring(2);
			System.out.println("[Opponent]: "+input);
		}else if(!yourTurn) {
			int move = Integer.parseInt(input);
			if(X) {
				spaces[move] = 'O';
			}else {
				spaces[move] = 'X';
			}
			moves++;
			paintBoard();
			System.out.println("Other player has played their turn choosing space "+move+"\nYour turn.");
			yourTurn = true;
			checkForWinner();
			if(!continueGame) {
				endGame();
			}
		}
	}
	/**
     * This method accepts an int which represents the space in the game grid where the player wishes to place their mark. 
     * If the space is available it will return true, if the space is taken it returns false. 
     *
     * @author Christian Neij
     */
	private boolean makeSureSpaceIsFree(int move) {
		if(spaces[move] == 'X' || spaces[move] == 'O') {
			return false;
		}
		return true;
	}
	/**
     * This method simply ends the game and shuts down the program. For the player reaching this method first it will simply end with a message.
     * For the other player an exception might be thrown before they reach the last line. 
     *
     * @author Christian Neij
     */
	public void endGame() {
		if(playerWon) {
			System.out.println("You win! Congratulations!");
		}else if(opponentWon) {
			System.out.println("Your opponent won, too bad!");
		}else if(tied) {
			System.out.println("Game tied! Good luck next time!");
		}else {
			System.out.println("ERROR: Something's gone wrong and the game has ended without results.");
		}
		try {
			Thread.sleep(1000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		sendingThread.kill();
		listenThread.kill();
		try {
			socket.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		System.exit(0);
	}
	/**
     * The threads are created and run. The game begins.  
     *
     * @author Christian Neij
     */
	public void runGame() {
		listenThread = new ListeningThread(this, socket);
		sendingThread = new SendingThread(this, socket);
		sendingThread.start();
		listenThread.start();
	}
	/**
     * A getter for the boolean value "yourTurn".
     *
     * @author Christian Neij
     */
	public boolean isYourTurn() {
		return yourTurn;
	}
	/**
     * A getter for the boolean value continueGame (though inverted).
     *
     * @author Christian Neij
     */
	public boolean gameIsOver() {
		return !continueGame;
	}
	/**
     * When the player enters a move this method evaluates whether it is acceptable and then carries out the move. 
     * It takes an int (the move) as parameter and returns true if the move was accepted, otherwise returns false.
     *
     * @author Christian Neij
     */
	public boolean acceptableMove(int move) {
		if(move < 9 && move > -1) {
			boolean spaceIsFree = makeSureSpaceIsFree(move);
			if(!spaceIsFree) {		
				return false;
			}
			if(X) {
				spaces[move] = 'X';
			}else {
				spaces[move] = 'O';
			}
			moves++;
			paintBoard();
			System.out.println("You've chosen to place your mark in space "+move+"\nOpponent's turn.");
			yourTurn = false;
			checkForWinner();
			return true;
		}
		return false;
	}
	/**
     * This method "paints" the board in the console for the players to see after every move so they need not remember the placements made. 
     *
     * @author Christian Neij
     */
	private void paintBoard() {
		String board = "";
		int count = 0;
		for(int i = 0; i<3; i++) {
			for(int j = 0; j<3;j++) {
				if(spaces[count] == 'X') {
					board = board+"X | ";
				}else if(spaces[count] == 'O') {
					board = board+"O | ";
				}else {
					board = board+"  | ";
				}
				count++;
			}
			board = board.substring(0, board.length() - 2);
			board = board+"\n";
		}
		System.out.println(board);
	}
	/**
     * Method evaluates whether anybody has won the game or not, if a winner is found the game will end and the winner will be announced.
     * Method also checks if board is filled without winner and will declare a tie with the game ending. 
     *
     * @author Christian Neij
     */
	private void checkForWinner() {
		for(int i=0; i<winningSpaces.length;i++) {
			if(spaces[winningSpaces[i][0]] == 'X' && spaces[winningSpaces[i][1]] == 'X' && spaces[winningSpaces[i][2]] == 'X') {
				if(X) {
					playerWon = true;
				}else {
					opponentWon = true;
				}
				continueGame = false;
			}else if(spaces[winningSpaces[i][0]] == 'O' && spaces[winningSpaces[i][1]] == 'O' && spaces[winningSpaces[i][2]] == 'O') {
				if(X) {
					opponentWon = true;
				}else {
					playerWon = true;
				}
				continueGame = false;
			}
		}
		if(!playerWon && !opponentWon && moves == 9) {
			tied = true;
			continueGame = false;
		}
	}
	/**
     * Main method, not much to say, it makes sure the amount of parameters from the user is acceptable and throws an IllegalArgumentException if not.
     *
     * @author Christian Neij
     */
	public static void main(String[] args) {
		if(args.length >= 3) {
			throw new IllegalArgumentException("Too many parameters, maximum of two allowed.");
		}
		if(args.length == 2) {
			new TicTacToe(args[0], Integer.parseInt(args[1]));
		}else if(args.length == 1) {
			new TicTacToe(Integer.parseInt(args[0]));
		}else {
			new TicTacToe();
		}
	}
}
