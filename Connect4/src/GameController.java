/**
 * Connect 4
 * By: Michael Jiang
 * Copyright Michael Jiang
 */

import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.*;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

//Main driver class that also creates buttons for the player to select his/her move
public class GameController {
	private JButton[] arrayBtn;
	private BufferedImage emptyTile;
	private BufferedImage yellowTile;
	private BufferedImage redTile;
	private JPanel panel;
	private JLabel [][] GUIBoard = new JLabel[8][8];
	private boolean playerTurn = true;
	//used to store tile positions on the game board
	//'Y' = yellow tile
	//'R' = red tile
	private char[][] board = new char[8][8];

	//Constructor for GUI Window
	public void GameWindow () {
		//setup image files
		//A blank tile image
		try {
			emptyTile = ImageIO.read(new File ("C:/Users/Michael/workspace/Connect4/src/Empty_Tile.png"));
		} catch (IOException e){
			System.out.println("Empty Tile: " + e.getMessage());
		}
		//A red tile image
		try {
			redTile = ImageIO.read(new File ("C:/Users/Michael/workspace/Connect4/src/Red_Tile.png"));
		} catch (IOException e){
			System.out.println("Red Tile: " + e.getMessage());
		}
		//A yellow tile image
		try {
			yellowTile = ImageIO.read(new File ("C:/Users/Michael/workspace/Connect4/src/Yellow_Tile.png"));
		} catch (IOException e){
			System.out.println("Yellow Tile: " + e.getMessage());
		}

		//create and set up the window
		JFrame frame = new JFrame("Connect 4");
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

		//create a JPanel for Gridlayout
		panel = new JPanel (new GridLayout(9, 8, 5, 5));

		//initialize 8 buttons for player controls (one for each column)
		arrayBtn = new JButton[8];

		// add JButtons dynamically with player ActionListeners
		for(int i = 0; i < 8; i++) {
			arrayBtn[i] = new JButton(Integer.toString(i+1));
			panel.add(arrayBtn[i]);
			//Action command will store the column that the player chose to put the tile in
			arrayBtn[i].setActionCommand(Integer.toString(i));
			//Add an action listener
			arrayBtn[i].addActionListener(new ActionListener (){
				@Override
				public void actionPerformed(ActionEvent e) {	
					//Check if the AI is still deciding a move
					if (playerTurn == true) {
						//First check if there is an empty tile space within the selected column
						if (EmptyColumn(board, Integer.parseInt(e.getActionCommand())) == true) {
							//Instantiate an AI opponent to play the oppponent move
							Opponent opponent = new Opponent(5);
							//Move the tile into the column that the player specified
							board = Move (Integer.parseInt(e.getActionCommand()), 'Y', board);
							//Player has finished turn
							playerTurn = false;
							//Generate an opponent move using the Opponent object, opponent
							board = opponent.OpponentMove(board);
							//Update and display the new board
							UpdateBoard ();
							//Player's turn again
							playerTurn = true;
							
							//Check for win
							if (Win (board) == 'Y') {
								JOptionPane.showMessageDialog(frame, "The player has won!");
							}
							else if (Win (board) == 'R') {
								JOptionPane.showMessageDialog(frame, "The AI has beaten the player!");
							}
						}
					}
				}
			});
		}
		
		//add the game board GUI under buttons
		//8 rows and 8 columns of empty tiles
		for(int i = 0; i < 8; i++) {
			for (int j = 0; j < 8; j++) {
				GUIBoard[i][j] = new JLabel(new ImageIcon (emptyTile));
				panel.add(GUIBoard[i][j]);
			}
		}

		//displays the window with a good fit for components within
		frame.add (panel);
		frame.pack();
		frame.setVisible(true);
	}

	/**
	 * ActionPerformed Methods
	 */

	//Gets the tile positions from board[][] and displays them graphically onto the screen
	public void UpdateBoard () {
		for(int i = 0; i < 8; i++) {
			for (int j = 0; j < 8; j++) {
				if (board[i][j] == 'Y') {
					GUIBoard[i][j].setIcon(new ImageIcon (yellowTile));
				}
				else if (board[i][j] == 'R') {
					GUIBoard[i][j].setIcon(new ImageIcon (redTile));
				}
			}
		}	
	}
	//Move method finds the lowest possible tile to stack new tile onto within specified column
	//@param column is the column to put tile in
	//@param colour is either 'Y' or 'R'
	//@param board is to-be-manipulated game board
	//@return new game board with move completed
	public char[][] Move (int column, char colour, char[][] board_) {
		char[][]newBoard = new char[8][8];
		
		//clone the board_ parameter to prevent manipulating that 2d array object accidentally
		for(int i = 0; i < 8; i++) {
		    newBoard[i] = board_[i].clone();
		}

		//Check for the lowest possible tile to stack the new tile onto
		//and add that new tile into the game board (matrix)
		for (int i = 7; i > -1; i--) {
			if (newBoard[i][column] != 'Y' && newBoard[i][column] != 'R') {
				newBoard[i][column] = colour;

				break;
			}
		}
		//return newly manipulated game board
		return newBoard;
	}

	//Check if there is still space in column
	//@param board_ is current game board
	//@param column is the column to scan
	public boolean EmptyColumn (char[][]board_, int column) {
		for (int i = 1; i < 8; i++) {
			if (board_[i][column] != 'Y' && board_[i][column] != 'R') {
				return true;
			}
		}
		return false;

	}

	//Scans for a win and returns 'Y', 'R', or 'N' for none
	//@param board_ is the game board with current tile positions
	//@return 'Y' for a yellow connect 4, 'R' for a red connect 4, or 'N' for no connect 4
	public char Win (char[][]board_) {
		//scan for horizontal win
		for (int i = 0; i < 8; i++) {
			for (int j = 0; j < 5; j++) {
				if (board_[i][j] == 'R' && board_[i][j + 1] == 'R' && board_[i][j + 2] == 'R' && board_[i][j + 3] == 'R') {
					return 'R';
				}
				else if (board_[i][j] == 'Y' && board_[i][j + 1] == 'Y' && board_[i][j + 2] == 'Y' && board_[i][j + 3] == 'Y') {
					return 'Y';
				}
			}
		}

		//scan for vertical win
		for (int i = 0; i < 5; i++) {
			for (int j = 0; j < 8; j++) {
				if (board_[i][j] == 'R' && board_[i + 1][j] == 'R' && board_[i + 2][j]  == 'R' && board_[i + 3][j] == 'R') {
					return 'R';
				}
				else if (board_[i][j] == 'Y' && board_[i + 1][j] == 'Y' && board_[i + 2][j] == 'Y' && board_[i + 3][j] == 'Y') {
					return 'Y';
				}
			}
		}
		//scan for diagonal with negative slope win
		for (int i = 0; i < 5; i++) {
			for (int j = 0; j < 5; j++) {
				if (board_[i][j] == 'R' && board_[i + 1][j + 1] == 'R' && board_[i + 2][j + 2]  == 'R' && board_[i + 3][j + 3] == 'R') {
					return 'R';
				}
				else if (board_[i][j] == 'Y' && board_[i + 1][j + 1] == 'Y' && board_[i + 2][j + 2] == 'Y' && board_[i + 3][j + 3] == 'Y') {
					return 'Y';
				}
			}
		}
		//scan for diagonal with positive slope win
		for (int i = 3; i < 8; i++) {
			for (int j = 0; j < 5; j++) {
				if (board_[i][j] == 'R' && board_[i - 1][j + 1] == 'R' && board_[i - 2][j + 2]  == 'R' && board_[i - 3][j + 3] == 'R') {
					return 'R';
				}
				else if (board_[i][j] == 'Y' && board_[i - 1][j + 1] == 'Y' && board_[i - 2][j + 2] == 'Y' && board_[i - 3][j + 3] == 'Y') {
					return 'Y';
				}
			}
		}

		return 'N';
	}

	//Driver method
	public static void main (String [] argv) {
		//Invoke the constructor through event-dispatching thread
		//for thread safety: Creates and shows this application's GUI
		GameController gameController = new GameController();
		javax.swing.SwingUtilities.invokeLater (new Runnable() {
			public void run() {
				gameController.GameWindow ();
			}
		});
	}
}
