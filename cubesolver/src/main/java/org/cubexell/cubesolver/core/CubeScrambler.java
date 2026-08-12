package org.cubexell.cubesolver.core;

import static org.cubexell.cubesolver.core.CubeConstants.POSSIBLE_MOVES;
import java.util.HashMap;
import java.util.Map;

public class CubeScrambler{
	private Robot robot;
	public CubeScrambler(){
	}

	public CubeScrambler(Robot robot){
		this.robot = robot;
	}

	protected String randomScrambleMove(){
		int randomIndex = (int) (Math.random() * 18);
		return POSSIBLE_MOVES[randomIndex];
	}

	protected char getFace(String move){
		return move.charAt(0);
	}

	public static final Map<Character, Character> scrambleDictionary = new HashMap<>();
	static{
		scrambleDictionary.put('U', 'D');
		scrambleDictionary.put('D', 'U');
		scrambleDictionary.put('R', 'L');
		scrambleDictionary.put('L', 'R');
		scrambleDictionary.put('F', 'B');
		scrambleDictionary.put('B', 'F');
	}


	public String[] getScramble(int numMoves){
		String[] scramble = new String[numMoves];
		scramble[0] = randomScrambleMove();
		String scrambleMove = randomScrambleMove();//so intellij stops complaining
		boolean getScrambleRepeat;
		for (int i = 1; i < numMoves; i++){
			getScrambleRepeat = true;
			while (getScrambleRepeat){
				scrambleMove = randomScrambleMove();
				if (getFace(scrambleMove) != getFace(scramble[i - 1])){
					if (i >= 2){
						if ((getFace(scramble[i - 1]) == scrambleDictionary.get(getFace(scrambleMove)))){
							if (getFace(scrambleMove) != getFace(scramble[i - 2])){
								getScrambleRepeat = false;
							}
						} else{
							getScrambleRepeat = false;
						}
					} else if (i == 1){//you can do just else here
						getScrambleRepeat = false;
					}
				}
			}
			scramble[i] = scrambleMove;
		}
		return scramble;


	//String getScramble_Move;
	//boolean getScramble_Repeat;
	//for (i = 0, i < numMoves, i++) {
	//getScramble_Repeat = true;
	//while (getScramble_Repeat) {
	//getScramble_Move = randomScrambleMove();
	//if (scramble.length != 0) {
	//int randomIndexTwo;
	//for (i = 0, i < 18, i++) {
	//if POSSIBLE_MOVES[i] == scramble[scramble.length - 1] {
	//randomIndexTwo = i;
	//}
	//}
	//randomIndex = (int) (randomIndex / 3);
	//randomIndexTwo = (int) (randomIndexTwo / 3);
	//if randomIndex != randomIndexTwo {
	//getScramble_Repeat = !getScramble_Repeat;
	//}
	//} else {
	//getScramble_Repeat = !getScramble_Repeat;
	//}
	//}
	//scramble[i] = getScramble_Move;
	//}
	}
	public char[][][] scramble(String[] scrambleMoves){
		Cube cube = new Cube(Helper.createSolvedCubeColors());
		cube.simulateMoves(scrambleMoves);
		if(robot!=null){
			robot.executeMoves(scrambleMoves);
		}
		return cube.getCubeColors();
	}

	public char[][][] randomScramble(){
		Cube cube = new Cube(Helper.createSolvedCubeColors());
		String[] scrambleMoves = getScramble(20);
		cube.simulateMoves(scrambleMoves);
		if(robot!=null){
			robot.executeMoves(scrambleMoves);
		}
		return cube.getCubeColors();
	}

}