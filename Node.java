package LayeredBFS;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.List;
import java.util.concurrent.DelayQueue;
import java.util.concurrent.ThreadLocalRandom;

public class Node extends Thread{
    private int uid; // uid of the current node
    private int leaderId;

    private int parentId;

    private boolean addedToBFS;
    private boolean iamLeader;
    private boolean knowMyChildren;

    private int numOfRejects;
    private int numOfAccepts;
    private int numOfNeighbors;
    private int messagesSent;

    private int numOfChildren;
    private int numOfChildConvergecasts;

    private int numOfNewNodesAdded;

    private boolean terminated;

    // Neighboring nodes of the node in the network topology
    public HashMap<Integer, Node> neighbors;

    public HashMap<Integer, Node> children;

    public HashMap<Integer, Node> parent;

    // Message queue for the current node
    private DelayQueue<DelayObject> delayQueue;



    public Node(int uid, int leaderID) {
        this.uid = uid;
        this.leaderId = leaderID;
        this.parentId = 0;

        // this.level = -1;
        // this.newPhase = -1;
        this.addedToBFS = false;
        this.iamLeader = false;
        this.knowMyChildren = false;

        this.numOfRejects = 0;
        this.numOfAccepts = 0;
        this.numOfNeighbors = 0;
        this.messagesSent = 0;

        this.numOfChildren = 0;
        this.numOfChildConvergecasts = 0;

        this.numOfNewNodesAdded = 0;

        this.terminated = false;

        this.delayQueue = new DelayQueue<>();
        this.neighbors = new HashMap<>();
        this.children = new HashMap<>();
        this.parent = new HashMap<>();
    }

    public void increaseNeighborCount(){
        this.numOfNeighbors += 1;
    }

    /**
     * LayeredBFS Algorithm
     */
    public void run() {
        System.out.println(this.uid + ": I have " + this.numOfNeighbors + " neighbors");
        DelayObject message = this.getMessage(this.uid, 0, "newPhase");
        boolean seeAcceptRejects = true;
        // Initially if you are the leader, get added to the BFS tree
        if(this.uid == this.leaderId){
            // this.level = 0;
            // this.newPhase = 0;
            this.addedToBFS = true;
            this.iamLeader = true;

            this.children = this.neighbors;
            this.numOfChildren = this.numOfNeighbors;

            // First newPhase search by the leader
            // this.newPhase += 1;

            // Send newPhase messages to each neighbor
            for(Map.Entry<Integer, Node> entry: children.entrySet()) {
             message = this.getMessage(this.uid, 0, "newPhase");
             entry.getValue().delayQueue.put(message);
             Main.incrementMessageCount();
         }
         System.out.println(this.uid + ": Leader has sent first phase messages to every one");
     }

     try {
            // Loop until process gets terminated
        while(!terminated){


                    // Read a message from the queue
         message = delayQueue.take();
         int p_uid = message.getPUid();
                // int c_uid = message.getCUid();
         int reqVal = message.getReqVal();
         String messageType = message.getMessageType();

                // Leader code
         if(iamLeader){

            if(messageType == "accept"){
                this.numOfNewNodesAdded += reqVal;
                this.numOfAccepts += 1;
                System.out.println(this.uid + ": Leader got accept message from " + p_uid + "with new nodes added " + reqVal);
            } else if(messageType == "newPhase"){
                Node entry = neighbors.get(p_uid);
                message = this.getMessage(this.uid, 0, "reject");
                entry.delayQueue.put(message);
                Main.incrementMessageCount();
                System.out.println(this.uid + ": Leader got newPhase message from " + p_uid);
            } else if(messageType == "convergeCast"){
                this.numOfNewNodesAdded += reqVal;
                this.numOfChildConvergecasts += 1;
                System.out.println(this.uid + ": Leader got convergeCast message form " + p_uid + " with " + reqVal + " new nodes added.");
            }

            if(numOfNeighbors == numOfChildConvergecasts + numOfAccepts){
                this.numOfAccepts = 0;
                this.numOfChildConvergecasts = 0;

                System.out.println(this.uid + ": Leader got replies messages from all neighbors");
                if(this.numOfNewNodesAdded == 0){

                    System.out.println(this.uid + ": Leader got no new additional nodes in this round. Termination Initiated.");
                            // print message in the future will be written\
                    String output = "\n\n******************************** FINAL OUTPUT OF LAYERED BFS ***********************************\n\n";
                    output += "NodeId: ";
                    output += Integer.toString(this.uid);
                    output += "; ChildrenId: ";

                    int childrenCount = 0;
                    for(Map.Entry<Integer, Node> entry: children.entrySet()) {
                        // System.out.print(entry.getKey() + " ");
                        output += Integer.toString(entry.getKey()) + " ";
                        message = this.getMessage(this.uid, reqVal, "BFSDone");
                        entry.getValue().delayQueue.put(message);
                        Main.incrementMessageCount();
                        childrenCount += 1;
                    }
                    if(childrenCount == 0){
                        output += "This is a leaf node. No children for this node.";
                    }
                    output += ";";
                    System.out.println(output);

                    this.setTermination();
                }

                else{
                    // newPhase += 1;
                    System.out.println(this.uid + ": Leader initiated a new phase.");
                    this.numOfNewNodesAdded = 0;
                    for(Map.Entry<Integer, Node> entry: children.entrySet()) {
                     message = this.getMessage(this.uid, 0, "newPhase");
                     entry.getValue().delayQueue.put(message);
                     Main.incrementMessageCount();
                 }
             }


         }
     }


                // Non Leader Code
     else{
        if(messageType == "newPhase"){
            if(!addedToBFS){
                addedToBFS = true;

                System.out.println(this.uid + ": Got request to be added to BFS Tree. Will be accepted.");
                            // set my level to the newPhase
                            // this.level = reqVal;

                            // Add parent to the parent variable
                parent.put(p_uid, neighbors.get(p_uid));

                this.parentId = p_uid;

                            // Send the Accept message to the parent
                Node entry  = parent.get(p_uid);
                message = this.getMessage(this.uid, 1, "accept");
                entry.delayQueue.put(message);
                Main.incrementMessageCount();
                continue;
            }
            else{
                System.out.println(this.uid + ": New phase request from " + p_uid);
                            // if not sent by parent then reject
                if(p_uid != this.parentId){
                                // Send the Reject message to the node
                    System.out.println(this.uid + ": Got request to be added to BFS Tree from " + p_uid + ". Will be rejected.");
                    Node entry  = neighbors.get(p_uid);
                    message = this.getMessage(this.uid, 0, "reject");
                    entry.delayQueue.put(message);
                    Main.incrementMessageCount();
                    continue;
                }

                else{
                                // reset the number of nodes added initially as a new phase has started
                    System.out.println(this.uid + ": Got request to carry on the newPhase of BFSTree from " + p_uid + ". Will be passed upon");
                    this.numOfNewNodesAdded = 0;
                    this.numOfAccepts = 0;
                    this.numOfRejects = 0;
                    this.numOfChildConvergecasts = 0;
                    this.messagesSent = 0;
                    if(!this.knowMyChildren){
                        this.knowMyChildren = true;

                        for(Map.Entry<Integer, Node> entry: neighbors.entrySet()) {
                            message = this.getMessage(this.uid, 0, "newPhase");
                            entry.getValue().delayQueue.put(message);
                            Main.incrementMessageCount();
                            this.messagesSent += 1;
                         }
                 }else{
                    for(Map.Entry<Integer, Node> entry: children.entrySet()) {
                     message = this.getMessage(this.uid, 0, "newPhase");
                     entry.getValue().delayQueue.put(message);
                     Main.incrementMessageCount();
                     this.messagesSent += 1;
                 }
                 // Incase if no children exist
                 if(this.messagesSent == 0){
                    Node entry = parent.get(this.parentId);
                    message = this.getMessage(this.uid, this.numOfNewNodesAdded, "convergeCast");
                    System.out.println(this.uid + ": Sent convergeCast message to " + this.parentId + " with new nodes added " + this.numOfNewNodesAdded);
                    entry.delayQueue.put(message);
                    Main.incrementMessageCount();
                 }
             }
             continue;
         }
     }
 }
 else if(messageType == "accept"){
    System.out.println(this.uid + ": Got accept message from " + p_uid + "with new nodes added " + reqVal);
                        // Add the node to the children map
    children.put(p_uid, neighbors.get(p_uid));

                        // Increase the numofnodesadded
    this.numOfNewNodesAdded += reqVal;

    this.numOfAccepts += 1;


} else if(messageType == "reject"){
    System.out.println(this.uid + ": Got reject message from " + p_uid + "with new nodes added " + reqVal);
    this.numOfRejects += 1;
} else if (messageType == "convergeCast"){
    System.out.println(this.uid + ": Got convergeCast message from " + p_uid + "with new nodes added " + reqVal);
    this.numOfChildConvergecasts += 1;
    this.numOfNewNodesAdded += reqVal;
} else if(messageType == "BFSDone"){
    String output = "NodeId: ";
    output += Integer.toString(this.uid);
    output += "; ChildrenId: ";

    int childrenCount = 0;
    for(Map.Entry<Integer, Node> entry: children.entrySet()) {
        // System.out.print(entry.getKey() + " ");
        output += Integer.toString(entry.getKey()) + " ";
        message = this.getMessage(this.uid, reqVal, "BFSDone");
        entry.getValue().delayQueue.put(message);
        Main.incrementMessageCount();
        childrenCount += 1;
    }
    if(childrenCount == 0){
        output += "This is a leaf node. No children for this node.";
    }
    output += ";";
    System.out.println(output);

    this.setTermination();
    continue;
}

if(this.messagesSent == this.numOfAccepts + this.numOfRejects + this.numOfChildConvergecasts){
    Node entry = parent.get(this.parentId);
    message = this.getMessage(this.uid, this.numOfNewNodesAdded, "convergeCast");
    System.out.println(this.uid + ": Sent convergeCast message to " + this.parentId + " with new nodes added " + this.numOfNewNodesAdded);
    entry.delayQueue.put(message);
    Main.incrementMessageCount();
}

}

}

} catch (InterruptedException e) {
    System.out.println("Exception "+e.getMessage());
}
}

    /**
     *
     * @param processId - Current node uid
     * @param maxUid - Maximum uid seen by current node
     * @param messageType - explore/reject/complete/leader
     * @return message to be sent with delay given the above parameters
     */
public DelayObject getMessage(int processId, int maxUid, String messageType) {
    // int randomDelay = 0;
    int randomDelay = ThreadLocalRandom.current().nextInt(1, 12);
    DelayObject message = new DelayObject(processId, maxUid, messageType, randomDelay);
    return message;
}

    /**
     *
     * @return Message queue buffer of the current node
     */
public DelayQueue<DelayObject> getMessageQueue() {
    return this.delayQueue;
}


    /**
     * Used for terminating the process
     */
public void setTermination() {
    this.terminated = true;
}

    // /**
    //  * @return total number of messages sent by node
    //  */
    // public int getTotalMessages() {
    //     return this.messages;
    // }

    /**
     * @return true if node is terminated else false
     */
public boolean isNodeTerminated() {
    return this.terminated;
}

}
