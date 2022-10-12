package LayeredBFS;

import java.io.PrintStream;
import java.util.*;
import java.io.File;
import java.io.FileNotFoundException;
import java.lang.Math;

public class Main {

    public static int messageCount = 0;

    public static void main(String[] args) throws FileNotFoundException {
        // Reading input file
        Scanner sc = new Scanner(new File("LayeredBFS/connectivity.txt"));


        System.out.println("******************* Start of the main code ************************");
        // Number of Processes
        int numProcesses = sc.nextInt();
        int uids[] = new int[numProcesses];

        // Getting the ID of the leader
        int leaderID = sc.nextInt();

        // Setting the UID's on our own
        for(int i = 0; i < numProcesses; i++)
            uids[i] = i+1;

        Node[] nodes = new Node[numProcesses];

        // Creating node objects
        for(int i = 0; i < numProcesses; i++) {
            nodes[i] = new Node(uids[i], leaderID);
        }

        // Reading the graph input and setting the neighbors for the processes
        for(int i = 0; i < numProcesses; i++) {
            HashMap<Integer, Node> neighbors = nodes[i].neighbors;
            for(int j = 0; j < numProcesses; j++) {
                if(sc.nextInt() == 1 && i != j) {
                    neighbors.put(uids[j], nodes[j]);
                    nodes[i].increaseNeighborCount();
                }
            }
        }

        // Printing to know if graph has been taken correctly or not
        for(int i = 0; i < numProcesses; i++){
            System.out.print("neighbors of node " + (i+1) + " are ");
            HashMap<Integer, Node> neighbors = nodes[i].neighbors;
            System.out.print(neighbors.keySet());
            System.out.println("");
        }


        // Start the node threads
        for(int i = 0; i < numProcesses; i++) {
            nodes[i].start();
        }

        // int totalMessages = 0;
        while(true) {
            boolean isRunning = false;
            for(int i = 0; i < numProcesses; i++) {
                if(!nodes[i].isNodeTerminated()) {
                    isRunning = true;
                }
            }
            if(!isRunning) {
                System.out.println("Main: All processes have been terminated.");
                System.out.println("Main: Total number of messages sent are " + messageCount);
                System.out.println("\n********************* END of LayredBFS *****************************\n");
                System.exit(0);
                return;
            }
        }
    }

    public static void incrementMessageCount(){
        messageCount += 1;
    }

}
