import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;

/**
 * This class extends Thread. It acts as conduit for receiving messages from other players. 
 * It is run in parallell with SendingThread which sends messages. 
 *
 * @author Christian Neij
 */
public class ListeningThread extends Thread {
	Socket socket;
	TicTacToe ttt;
	BufferedReader reader;
	boolean run = true;
	
	/**
     * The constructor takes two parameters, the TicTacToe game session the thread belongs to and the socket that connects to the other player.
     *
     * @author Christian Neij
     */
	public ListeningThread(TicTacToe ttt, Socket socket) {
		this.ttt = ttt;
		this.socket = socket;
		try {
			reader = new BufferedReader(new InputStreamReader(this.socket.getInputStream()));
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	/**
     * The run()-method of this thread-class waits for messages from the other player as well as their moves and send them forward.
     *
     * @author Christian Neij
     */
	public void run() {
		while(run) {
			try {
                String otherPlayersOutput = reader.readLine();
                ttt.inputFromOtherUser(otherPlayersOutput);
            } catch (IOException e) {
                System.out.println("Error reading from other player: " + e.getMessage());
                e.printStackTrace();
                break;
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
