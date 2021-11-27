import java.io.Console;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;

/**
 * This class extends Thread. It acts as conduit for sending messages to other players. 
 * It is run in parallell with ListeningThread which receives messages. 
 *
 * @author Christian Neij
 */
public class SendingThread extends Thread {
	private TicTacToe ttt;
	private Socket socket;
	private PrintWriter writer;
	boolean run = true;
	
	/**
     * The constructor takes two parameters, the TicTacToe game session the thread belongs to and the socket that connects to the other player.
     *
     * @author Christian Neij
     */
	public SendingThread(TicTacToe ttt, Socket socket) {
		this.ttt = ttt;
		this.socket = socket;
		try {
			writer = new PrintWriter(this.socket.getOutputStream(), true);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	/**
     * The run()-method of this thread-class waits for the player to write in the console and handles the written content as is appropriate. 
     * Messages starting with "M:" are sent forward to the other player as messages while other messages will be considered moves.  
     *
     * @author Christian Neij
     */
	public void run() {
		Console console = System.console();
		String playerInput;

		while(run) {
			playerInput = console.readLine();
			if(playerInput.startsWith("M:")) {
				writer.println(playerInput);
			}else if(ttt.isYourTurn()) {
				int move;
				try {
					move = Integer.parseInt(playerInput);
				}catch(NumberFormatException e) {
					continue;
				}
				boolean acceptableMove = ttt.acceptableMove(move);
				if(acceptableMove) {
					writer.println(playerInput);
				}
				if(ttt.gameIsOver()) {
					ttt.endGame();
				}
			}
		}
	}
	/**
     * This method ends the loop in the run()-method, effectively killing the thread.
     *
     * @author Christian Neij
     */
	public void kill() {
		run = false;
	}
}
