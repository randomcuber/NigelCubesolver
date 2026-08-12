import org.cubexell.cubesolver.core.*;

import java.io.IOException;
import java.util.Scanner;
import java.io.Console;

import com.pi4j.Pi4J;

import com.pi4j.io.gpio.*;

import java.util.Arrays;
import java.util.concurrent.locks.LockSupport;




public class Main {
    public static void main(String[] args) throws IOException, InterruptedException {
        Scanner scanner = new Scanner(System.in);
        String method = "B";
        String isScramblingCube = "Y";
        boolean autoTune = false;

        String cubeSolvingMethod;

        if (args.length > 2 && args[0] != null) {
            method = args[0];
            isScramblingCube = args[1];
            if (args[2] != null){
                if(args[2].equalsIgnoreCase("Y")){
                    autoTune = true;
                    System.out.println("autoTuning");
                }
            }
        }

        if ("O".equalsIgnoreCase(method)) {
            cubeSolvingMethod = "Old Pochmann";
        }
        else if ("B".equalsIgnoreCase(method)) {
            cubeSolvingMethod = "Beginner";
        }
        else {
            cubeSolvingMethod = "Kociemba";
        }

        Motor upMotor = new RohsStepperMotor(2, 3, 4, 17);
        Motor downMotor = new RohsStepperMotor(14, 15, 18, 23);
        Motor rightMotor = new RohsStepperMotor(5, 6, 13, 19);
        Motor leftMotor = new RohsStepperMotor(22, 10, 9, 11);
        Motor frontMotor = new RohsStepperMotor(24, 25, 8, 7);
        Motor backMotor = new RohsStepperMotor(12, 16, 20, 21);

//        Motor upMotor = new RohsStepperMotor (24, 25, 8, 7);
//		Motor downMotor = new RohsStepperMotor (1, 12, 16, 20);
//		Motor rightMotor = new RohsStepperMotor (21, 26, 19, 13);
//		Motor leftMotor = new RohsStepperMotor (2, 3, 4, 17);
//		Motor frontMotor = new RohsStepperMotor (6, 5, 0, 11);
//		Motor backMotor = new RohsStepperMotor (9, 10, 22, 27);

        Robot robot = new RaspberryPiRobot(upMotor, downMotor, rightMotor, leftMotor, frontMotor, backMotor);

//        for (int i=0;i<100;i++){
//            robot.U();
//            robot.D();
//            robot.R();
//            robot.L();
//            robot.F();
//            robot.B();
//        }


//        System.exit(0);

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            System.out.println("Shutdown hook triggered");
            System.out.flush();
            try{
                robot.resetMotors();
            } catch(Exception e){
                System.out.println("exception occured while trying to reset motors"+e);
            }
            System.out.flush();
            try{
                Thread.sleep(2000);
            } catch(InterruptedException ignored){}
        }));


        System.out.println("Cube solving method: " + cubeSolvingMethod);


        CubeScrambler scrambler = new CubeScrambler(robot);

        Cube cube;

        CubeColorInspector inspector = new OpenCvRaspberryPiCamera(robot, autoTune);
        char[][][] cubeColors;

        //String[] scrambleMoves = {"Ri","Bi","Ri","F2","L","F2","B","R","Ui","R","Fi","U","Li","B","L2","U2","B2","F2","L2","F"};

        if ("Y".equalsIgnoreCase(isScramblingCube)) {
            String[] scrambleMoves = scrambler.getScramble(20);
            System.out.println("------Scramble-------------");
            for (String scrambleMove : scrambleMoves) {
                System.out.print(scrambleMove + ",");
            }
            cubeColors = scrambler.scramble(scrambleMoves);
            Thread.sleep(5000);
        }
        else {
            Process preview = new ProcessBuilder(
                    "rpicam-hello",
                    "--qt-preview",
                    "-t", "0"
            ).start();
            System.out.println("Rotate polarizer to minimize glare. Press Enter to proceed...");
            scanner.nextLine();

            preview.destroyForcibly();
            preview.waitFor();
            Thread.sleep(500);

            cubeColors = inspector.inspect();
            if(autoTune){
                System.exit(0);
            }
        }


        //char[][][] fixedCube = CubeFixer.fixCube(cubeColors);

        cube = new Cube(cubeColors);

        System.out.println("------After scramble------------");
        System.out.println(cube.toString());

        CubeSolvingMethod solver;

        if ("Beginner".equalsIgnoreCase(cubeSolvingMethod)) {
            solver = new BeginnerMethod();
        }
        else if ("Kociemba".equalsIgnoreCase(cubeSolvingMethod)) {
            solver = new KociembaAlgorithm();
        }
        else {
            solver = new OldPochmannMethod();
        }

        String[] solution = solver.solve(cubeColors);



        System.out.println("------solution num moves: " + solution.length + "------------");
        for (String s : solution) {
            System.out.print(s + ",");
        }
        System.out.println();
        String[] simplifiedSolution = cube.simplifySolution(solution);
        System.out.println("------simplified solution num moves: " + simplifiedSolution.length + "------------");
        for (String s : simplifiedSolution) {
            System.out.print(s + ",");
        }

        cube.simulateMoves(simplifiedSolution);
        System.out.println("------AfterSolving------------");
        System.out.println(cube.toString());

        robot.executeMoves(simplifiedSolution);
        robot.resetMotors();

        System.out.println("finished");

    }

}
