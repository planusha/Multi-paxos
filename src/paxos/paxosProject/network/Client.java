package paxosProject.network;

import paxosProject.Configuration;
import paxosProject.network.messages.LeaderInfo;
import paxosProject.network.messages.Message;
import paxosProject.network.messages.Put;
import paxosProject.network.messages.Response;

import java.beans.*;
import java.nio.channels.ClosedChannelException;
import java.util.HashMap;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by anusha on 4/23/16.
 */
public class Client implements EventHandler {

    NodeIdentifier myID = null;
    Network network = null;
    Timer te;
    NodeIdentifier leader;
    public Client(NodeIdentifier myID){
        this.myID = myID;
        network = new NettyNetwork(myID, this);
        leader = Configuration.proposerIDs.get(1);
    }
    private int requestCount = 0;
    private HashMap<Integer, Timer> requests_timer = new HashMap<Integer,Timer>();

    public void sendValue(HashMap hs){
        requestCount++;
        te = new Timer();
        te.schedule(new RequestTimer(new Put(myID,requestCount,hs), myID, leader, network), 1000, 10000);
        requests_timer.put(requestCount,te);
    }

    @Override
    public void handleMessage(Message msg) {
        System.out.println("CLIENT recieved " + msg);
        if (msg instanceof Response) {
          this.handleResponse(msg);
        }
        else if(msg instanceof LeaderInfo){
            this.handleLeaderInfo(msg);
        }

    }

    public void setLeader(NodeIdentifier leader){
        this.leader = leader;
    }

    private void handleLeaderInfo(Message msg){
        setLeader(((LeaderInfo) msg).getLeader());
        resendRequest(((LeaderInfo) msg).getIndex(), ((LeaderInfo) msg).getValue());

    }

    private void resendRequest(int requestID, HashMap hs){
        te = requests_timer.get(requestID);
        try{
            te.cancel();
            te.purge();
            te = null;
        }catch (NullPointerException ne){
            ne.printStackTrace();
        }
        te = new Timer();
        te.schedule(new RequestTimer(new Put(myID,requestID,hs), myID, leader, network), 2000, 10000);
        requests_timer.put(requestID,te);
    }

    private void handleResponse(Message msg){
       // System.out.printf("%s receive %s\n", myID, (Response) msg);
        Integer index = ((Response) msg).getIndex();
        te = requests_timer.get(index);
        try{
            te.cancel();
            te = null;
        }catch (NullPointerException ne){
            ne.printStackTrace();
        }
    }

    @Override
    public void handleTimer() {

    }

    @Override
    public void handleFailure(NodeIdentifier node, Throwable cause) {
        if (cause instanceof ClosedChannelException) {
            System.out.printf("%s handleFailure get %s\n", myID, cause);
        }
    }


    public class RequestTimer extends TimerTask{

        Message msg;
        NodeIdentifier myID = null;
        NodeIdentifier receiverID = null;
        Network network = null;



        public RequestTimer(Message msg, NodeIdentifier myID, NodeIdentifier receiverID, Network nettyNetwork){
            this.msg = msg;
            this.myID = myID;
            this.receiverID = receiverID;
            this.network= nettyNetwork;
        }

        @Override
        public void run() {
            System.out.printf("client sending %s => %s\n", msg, receiverID);
            network.sendMessage(receiverID, msg);
        }
    }

}
