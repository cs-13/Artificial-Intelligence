import java.util.Scanner;
import java.util.ArrayList;

public class Hexed {
    Scanner sc = new Scanner(System.in);
    public static void main(String[] args) {
        try {
            Hexed test = new Hexed();
            test.run();
        } catch(Exception e) {
            e.printStackTrace();
        }
    }
    
    public void run() {
        char[][] array = new char[7][9];
        Board board = new Board(0, null, array);
        System.out.println("--------Hexed Helper--------");
        System.out.print("Enter Starting Column: ");
        int col = sc.nextInt();
        System.out.print("Enter Starting Row: ");
        int row = sc.nextInt();
        System.out.print("Enter Team 1's Color [r/g]: ");
        char teamOneColor = sc.next().charAt(0);
        System.out.print("Enter Team 2's Color [r/g]: ");
        char teamTwoColor = sc.next().charAt(0);
		System.out.print("Enter Top Color [r/g]: ");
        char top = sc.next().charAt(0);
        System.out.print("Enter Team to Move 1st [1/2]: ");
        int first = sc.nextInt();
        char turnColor = (first == 1)? teamOneColor : teamTwoColor;
        char waitColor = (first == 1)? teamTwoColor : teamOneColor;
        System.out.print("Enter Team to Help [1/2]: ");
        int ourTeam = sc.nextInt();
        
        board.populate(row, col, Character.toLowerCase(top));
        display(board);
        
        int turnTeam = 0;
        int choice = 0;
        boolean isOneHexed = false;
        boolean isTwoHexed = false;
        ArrayList<Move> possibleMoves = new ArrayList<Move>();
        
        do {
        	if (turnColor == teamOneColor) {
                turnTeam = 1;
        	} else if (turnColor == teamTwoColor){
                turnTeam = 2;
        	}
        	
            System.out.println("---------------------------");
            System.out.println("     Turn: Team "+turnTeam+" ["+turnColor+"]");
            System.out.println("---------------------------");
            possibleMoves = board.getPossibleMoves(turnColor, waitColor);
            if (possibleMoves != null) {
                System.out.println("      Possible Moves");
                System.out.println("---------------------------");
                for(int x = 0; x < possibleMoves.size(); x++) {
                	System.out.println("["+x+"]  Column: "+ possibleMoves.get(x).getTile().getCol() + "; Row: " + possibleMoves.get(x).getTile().getRow());
                }
                System.out.println("---------------------------");
                if (ourTeam == turnTeam) {
                    System.out.println("      Suggested Move");
                    System.out.println("---------------------------");
                	Move suggestedMove = suggestMove(possibleMoves, board, turnColor, waitColor);
					int counter = 0;
					boolean continues = true;
					while(continues && counter < possibleMoves.size()){
						if(suggestedMove.equals(possibleMoves.get(counter))){
							continues = false;
						}else{
							counter++;
						}
					}
                	System.out.println("["+(counter)+"]  Column: "+ suggestedMove.getTile().getCol() + "; Row: " + suggestedMove.getTile().getRow());
                    System.out.println("---------------------------");
                }
                System.out.print("Enter Move: ");
            	choice = sc.nextInt();
            	board.makeMove(possibleMoves.get(choice));
                possibleMoves = null;
            	if(turnTeam == 1) {
            		turnColor = teamTwoColor;
            		waitColor = teamOneColor;
            		isOneHexed = false;
            	} else if(turnTeam == 2) {
            		turnColor = teamOneColor;
            		waitColor = teamTwoColor;
            		isTwoHexed = false;
            	}
            } else {
                System.out.println("           HEXED");
                System.out.println("---------------------------");
                System.out.println("Press Enter to Continue...");
                sc.nextLine();
                sc.nextLine();
                System.out.println();
            	if(turnTeam == 1) {
            		turnColor = teamTwoColor;
            		waitColor = teamOneColor;
            		isOneHexed = true;
            	} else if(turnTeam == 2) {
            		turnColor = teamOneColor;
            		waitColor = teamTwoColor;
            		isTwoHexed = true;
            	}
            }
            display(board);
            
        } while(!(isOneHexed && isTwoHexed));
        
        sc.close();
    }

	private void display(Board board) {
		System.out.println();
        System.out.println("-----------Board-----------");
        board.showBoard();
        System.out.println("---------------------------");
        int g = board.playerChipsCounter('g');
        int r = board.playerChipsCounter('r');
        System.out.printf("Green Chips: %d%nRed Chips: %d%n",g,r);
        System.out.println("---------------------------");
        System.out.println("Press Enter to Continue...");
        sc.nextLine();
        sc.nextLine();
        System.out.println();
	}

	private Move suggestMove(ArrayList<Move> possibleMoves, Board board, char turnColor, char waitColor) {
		ArrayList<Board> frontier = new ArrayList<Board>();
		frontier.addAll(convertToBoard(possibleMoves, board));
		ArrayList<Board> tempFrontier = new ArrayList<Board>();
		int counter = 4;
		boolean notLevel = false;
        System.out.print("Loading");
		do{
			notLevel = false;
			for(int x = 0; x < frontier.size(); x++) {
				tempFrontier.add(frontier.get(x).clone());
			}
			if(counter == 0){
				tempFrontier.clear();
				tempFrontier.addAll(frontier);
				System.out.println();
				break;
			}
			for(int x = 0; x < counter; x++) { //Populate frontier with the successors at depth 6 (counter)
				int size = tempFrontier.size();
				int y = 0;
				while(y < size){
					ArrayList<Move> temp = new ArrayList<Move>();
					if(x % 2 == 0) {
						temp = tempFrontier.get(y).getPossibleMoves(waitColor, turnColor);
						if(temp != null) {
							tempFrontier.addAll(convertToBoard(temp, tempFrontier.get(y)));
						} else {
							counter = x;
							notLevel = true;
							break; 
						}
					} else {
						temp = tempFrontier.get(y).getPossibleMoves(turnColor, waitColor);
						if(temp != null) {
							tempFrontier.addAll(convertToBoard(temp, tempFrontier.get(y)));
						} else {
							counter = x;
							notLevel = true;
							break;
						}
					}
					y++;
				}
				if(notLevel == true) {
					tempFrontier.clear();
					break;
				}
				for(int z = 0; z < y; z++) {
					tempFrontier.remove(0);
				}
			}
	        System.out.print(".");
		} while (notLevel == true);
		
		

		for(int x = 0; x < tempFrontier.size(); x++) { //set heuristic costs of items in the populated frontier
			if (turnColor == 'r') {
				tempFrontier.get(x).setEvaluation(tempFrontier.get(x).playerChipsCounter('r') - tempFrontier.get(x).playerChipsCounter('g'));
			} else if (turnColor == 'g') {
				tempFrontier.get(x).setEvaluation(tempFrontier.get(x).playerChipsCounter('g') - tempFrontier.get(x).playerChipsCounter('r'));
			}
		}
		
		
		ArrayList<Board> parent = new ArrayList<Board>();
		if (counter == 0){
			parent.addAll(tempFrontier);

		} else{
			ArrayList<Board> tempList = new ArrayList<Board>();
			Board tempBoard = new Board();

			for (int x = counter; x > 0; x--) { //Apply min-max algorithm
				tempFrontier.addAll(parent);
				parent.clear();
				int y = 0;
				do {
					tempFrontier.addAll(tempList);
					tempList.clear();
					tempBoard = tempFrontier.remove(0);
					parent.add(tempBoard.getParent());
					int heuristic = tempBoard.getEvaluation();
					while(!(tempFrontier.isEmpty())) {
						if (tempBoard.getParent().equals(tempFrontier.get(0).getParent())) {
							if(x % 2 == 0) {
								if(heuristic < tempFrontier.get(0).getEvaluation()) {
									heuristic = tempFrontier.remove(0).getEvaluation();
								} else {
									tempFrontier.remove(0);
								}
							} else {
								if(heuristic > tempFrontier.get(0).getEvaluation()) {
									heuristic = tempFrontier.remove(0).getEvaluation();
								} else {
									tempFrontier.remove(0);
								}
							}
						} else {
							tempList.add(tempFrontier.remove(0));
						}
						
					} //already got heuristic cost for one parent
					parent.get(y).setEvaluation(heuristic);
					y++;
				} while(!(tempList.isEmpty())); //got all the states at previous depth
				System.out.print(".");
			}
			System.out.println();
		}
		
		
		Move suggestedMove = new Move();
		ArrayList<Move> listMove = new ArrayList<Move>();
		int count = parent.size();
		 while (count > 0){
			Board tempB = parent.remove(0);
			Move tempM = possibleMoves.remove(0);
			if(!(parent.isEmpty())){
				for(int y = 0; y < count-1; y++) {
					if (tempB.getEvaluation() > parent.get(y).getEvaluation()) {
						suggestedMove = possibleMoves.get(y).clone();
					} else if(tempB.getEvaluation() < parent.get(y).getEvaluation()){
						suggestedMove = tempM.clone();
					} else if((tempB.getEvaluation() == parent.get(y).getEvaluation()) && !(tempB.equals(parent.get(y))) ){
						suggestedMove = tempM.clone();
						listMove.add(suggestedMove);
					} else {
						suggestedMove = tempM.clone();
					}
				}
				parent.add(tempB);
				possibleMoves.add(tempM);
				count--;
			}else {
				suggestedMove = tempM.clone();
				parent.add(tempB);
				possibleMoves.add(tempM);
				count--;
				break;
			}
		}
		 
		if (!(listMove.isEmpty())) {
			Move lastMove = new Move();
			for(int x = 0; x < listMove.size(); x++) {
				if (convertToBoard(suggestedMove, board, turnColor).getEvaluation() > convertToBoard(listMove.get(x), board, turnColor).getEvaluation()) {
					lastMove = listMove.get(x).clone();
				} else {
					lastMove = suggestedMove.clone();
				}
			}
			return lastMove;
		} else {
			return suggestedMove;
		}
	}
	
	private ArrayList<Board> convertToBoard(ArrayList<Move> possibleMoves, Board board) {
		ArrayList<Board> listBoard = new ArrayList<Board>();
		Board newBoard = new Board();
		
		for(int x = 0; x < possibleMoves.size(); x++) {
			newBoard = board.clone();
			newBoard.setDepth(board.getDepth()+1);
			newBoard.setParent(board);
			listBoard.add(newBoard.makeMove(possibleMoves.get(x)).clone());
		}
		return listBoard;
	}
	
	private Board convertToBoard(Move possibleMove, Board board, char turnColor) {
		Board newBoard = new Board();
		newBoard = board.clone();
		newBoard.makeMove(possibleMove);
		if (turnColor == 'r') {
			newBoard.setEvaluation(newBoard.playerChipsCounter('r') - newBoard.playerChipsCounter('g'));
		} else if (turnColor == 'g') {
			newBoard.setEvaluation(newBoard.playerChipsCounter('g') - newBoard.playerChipsCounter('r'));
		}
		return newBoard;
	}
}