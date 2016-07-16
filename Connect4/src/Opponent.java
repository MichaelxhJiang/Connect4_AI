import java.util.ArrayList;

class TestBoard {
	char[][]board;	//Board with test positions
	char player;	//Yellow or Red move
	int column;		//Original column 

	public TestBoard (char[][]boardCopy, char player_, int column_) {
		board = boardCopy;
		player = player_;
		column = column_;
	}
}

class SplitSearch implements Runnable {
	public ArrayList <TestBoard> threadTestBoard = new ArrayList<TestBoard>();
	public ArrayList <TestBoard> parentTestBoard;
	public char player;
	public int[] columnScore;
	private GameController gameController = new GameController ();
	private Opponent opponent = new Opponent(0); //Need to use the ThreeInARows method
	private int minRange;
	private int maxRange;
	public boolean AIFirstMove = true;

	public SplitSearch (ArrayList<TestBoard> currentTestBoard, int[] columnScore_, int minRange_, int maxRange_) {
		parentTestBoard = currentTestBoard;
		columnScore = columnScore_;
		minRange = minRange_;
		maxRange = maxRange_;
	}
	@Override
	public void run() {
		int size = parentTestBoard.size();
		for (int j = 0; j < size; j++) {
			TestBoard currentBoard = parentTestBoard.get(j);
			//Check which player's turn it is (opposite of board.player)
			char player;
			if (currentBoard.player == 'R') {
				player = 'Y';
			} else {
				player = 'R';
			}
			//Create all 4 moves on left side for each board layout
			for(int k = minRange; k < maxRange; k++) {
				if (gameController.EmptyColumn(currentBoard.board, k) == true) {
					threadTestBoard.add ( new TestBoard(gameController.Move(k, player, currentBoard.board), player, currentBoard.column));
					//If play results in win, stop layering this play since play is finished
					if (gameController.Win(threadTestBoard.get(threadTestBoard.size() - 1).board) == player) {
						if (player == 'R') {
							columnScore[currentBoard.column] += 1000;
						} else {
							columnScore[currentBoard.column] -= 1000;
							//If occurs right after first AI move, kill it
							if (AIFirstMove) {
								columnScore[currentBoard.column] -= 2147400000;	//close to maxed out int
							}
						}
						threadTestBoard.remove(threadTestBoard.size() - 1);
					}
				}
			}
		}
		AIFirstMove = false;
		//CHECK FOR THREE IN A ROWS FOR EACH BOARD OUTCOME WITHIN threadTestBoard's
		for (int i = 0; i < threadTestBoard.size(); i++) {
			int[] yr = opponent.CheckThreeInARows (threadTestBoard.get(i).board);
			columnScore[threadTestBoard.get(i).column] += (yr[1] - yr[0])*500;
		}
	}
}

public class Opponent {
	private int searchDepth;

	private GameController gameController = new GameController();

	public Opponent (int sd) {
		searchDepth = sd;
	}

	//scan for 3-in-a-rows in board (Used when AI considers moves)
	//@return array with first element num of yellow 3 in a rows and last element num of red 3 in a rows
	public int[] CheckThreeInARows(char[][] boardCopy) {
		char[][] scannedBoard = boardCopy;

		//first element is num of yellow three-in-a-rows
		//second element is num of red three-in-a-rows
		int[] yr = new int[2];
		yr[0] = 0;
		yr[1] = 0;

		//scan horizontal rows
		for (int i = 0; i < 8; i++) {
			for (int j = 0; j < 5; j++) {
				int numReds = 0;
				int numYellows = 0;

				//scan 4 tiles
				for (int k = 0; k < 4; k++) {
					if (scannedBoard [i][j+k] == 'Y') { 
						numYellows++;
					}
					else if(scannedBoard [i][j+k] == 'R') {
						numReds++;
					}
				}
				if ((numYellows == 3 && numReds == 0) ) {
					yr[0]++;
				}
				else if (numReds == 3 && numYellows == 0) {
					yr[1]++;
				}
			}
		}

		//scan vertical columns
		for (int i = 0; i < 5; i++) {
			for (int j = 0; j < 8; j++) {
				int numReds = 0;
				int numYellows = 0;

				//scan 4 tiles
				for (int k = 0; k < 4; k++) {
					if (scannedBoard [i+k][j] == 'Y') { 
						numYellows++;
					}
					else if(scannedBoard [i+k][j] == 'R') {
						numReds++;
					}
				}
				if ((numYellows == 3 && numReds == 0) ) {
					yr[0]++;
				}
				else if (numReds == 3 && numYellows == 0) {
					yr[1]++;
				}
			}
		}
		//scan diagonal with negative slope
		for (int i = 0; i < 5; i++) {
			for (int j = 0; j < 5; j++) {
				int numReds = 0;
				int numYellows = 0;

				//scan 4 tiles
				for (int k = 0; k < 4; k++) {
					if (scannedBoard [i+k][j+k] == 'Y') { 
						numYellows++;
					}
					else if(scannedBoard [i+k][j+k] == 'R') {
						numReds++;
					}
				}
				if ((numYellows == 3 && numReds == 0) ) {
					yr[0]++;
				}
				else if (numReds == 3 && numYellows == 0) {
					yr[1]++;
				}
			}
		}
		//scan diagonal with positive slope
		for (int i = 3; i < 8; i++) {
			for (int j = 0; j < 5; j++) {
				int numReds = 0;
				int numYellows = 0;

				//scan 4 tiles
				for (int k = 0; k < 4; k++) {
					if (scannedBoard [i-k][j+k] == 'Y') { 
						numYellows++;
					}
					else if(scannedBoard [i-k][j+k] == 'R') {
						numReds++;
					}
				}
				if ((numYellows == 3 && numReds == 0) ) {
					yr[0]++;
				}
				else if (numReds == 3 && numYellows == 0) {
					yr[1]++;
				}
			}
		}
		return yr;
	}

	//Returns board with move played
	public char[][] OpponentMove (char[][]boardCopy) {
		char[][] movedBoard;
		int[] columnScore = new int[8];

		//init scores of each column to 0
		for (int i = 0; i < 8; i++) {
			columnScore[i] = 0;
		}

		//boards that will hold the minimax search results
		ArrayList <TestBoard> testBoard = new ArrayList <TestBoard>();

		//Check if the player is about to win before even using minimax search
		//if true, immediately block that column
		int[] initialYR = CheckThreeInARows (boardCopy);
		if (initialYR[0] > 0) {
			for (int i = 0; i < 8; i++) {
				if (gameController.Win(gameController.Move(i, 'Y', boardCopy)) == 'Y') {
					movedBoard = gameController.Move(i, 'R', boardCopy);
					return movedBoard;
				}
			}
		}

		//first layer of search
		for (int i = 0; i < 8; i++) {
			if (gameController.EmptyColumn(boardCopy, i) == true) {
				testBoard.add( new TestBoard(gameController.Move(i, 'R', boardCopy), 'R', i));
				//Check if first move results in a win
				//If true, automatically move here
				if (gameController.Win(testBoard.get(testBoard.size() - 1).board) == 'R') {
					movedBoard = gameController.Move(i, 'R', boardCopy);
					return movedBoard;
				}
			}
			//Check for three in a rows for each board outcome within initial testBoard's (starting with first layer)
			int[] yr = CheckThreeInARows (testBoard.get(testBoard.size() - 1).board);
			columnScore[testBoard.get(testBoard.size() - 1).column] += (yr[1] - yr[0])*500;
		}
		//Scan until search depth is reached
		for (int i = 1; i < searchDepth; i++) {
			//Initiallize the threads
			SplitSearch leftSide = new SplitSearch (testBoard, columnScore, 0, 4);
			Thread t1 = new Thread (leftSide, "Thread-1");
			SplitSearch rightSide = new SplitSearch (testBoard, columnScore, 4, 8);
			Thread t2 = new Thread (rightSide, "Thread-2");
			
			//Tell SplitSearch classes if the next move they generate is right after the AI's first possible move
			if (i == 1) {
				leftSide.AIFirstMove = true;
				rightSide.AIFirstMove = true;
			} else {
				leftSide.AIFirstMove = false;
				rightSide.AIFirstMove = false;
			}
			
			//Start the threads
			t1.start();
			t2.start();
			//Wait for threads to synchronize
			try {

				t1.join(); 
				t2.join();
			} catch( Exception e) {
				System.out.println("Interrupted");
			}

			//Remove old parent boards before adding new wave of boards
			testBoard.removeAll(testBoard);
			//Retrieve the game boards generated by the threads
			for (int j = 0; j < leftSide.threadTestBoard.size(); j++) {
				testBoard.add(leftSide.threadTestBoard.get(j));
			}
			for (int j = 0; j < rightSide.threadTestBoard.size(); j++) {
				testBoard.add(rightSide.threadTestBoard.get(j));
			}
		}

		//Find best column to move
		int highestColumn = -1;
		int highestColumnScore = Integer.MIN_VALUE;
		for (int i = 0; i < 8; i++) {
			//Check if move is even possible
			if (gameController.EmptyColumn(boardCopy, i) == true) {
				if (columnScore[i] > highestColumnScore) {
					highestColumnScore = columnScore[i];
					highestColumn = i;
				}
			}
		}
		movedBoard = gameController.Move(highestColumn, 'R', boardCopy);
		return movedBoard;
	}

}
