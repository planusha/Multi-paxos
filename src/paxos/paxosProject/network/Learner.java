package paxosProject.network;

import com.sun.org.apache.xml.internal.utils.res.IntArrayWrapper;
import paxosProject.Configuration;
import paxosProject.network.messages.*;

import java.nio.channels.ClosedChannelException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

/**
 * Created by anusha on 4/20/16.
 */
public class Learner implements EventHandler {

    private int slotNumber;
    private int prev_slotNumber;
    private ArrayList<HashMap<Integer, Integer>> values = new ArrayList<>();
    private ArrayList<HashMap<Integer, Integer>> key_values = new ArrayList<>();
    private HashSet<Integer> processedRequests = new HashSet<>();
    Boolean fail_send_to_client;


    NodeIdentifier myID = null;
    Network network = null;
    public Learner(NodeIdentifier myID){
        this.myID = myID;
        network = new NettyNetwork(myID, this);
        fail_send_to_client = false;
    }

    @Override
    public void handleMessage(Message msg) {
        if(msg instanceof Learn){
            this.handleLearn(msg);
        }
        else if(msg instanceof Update){
            System.out.printf("%s receive %s\n", myID, (Update) msg);
            key_values  = ((Update) msg).getValues();
        }
    }

    private void handleUpdate(Message msg){

    }

    private void handleLearn(Message msg){
        int requestID = ((Learn) msg).getIndex();
        if(processedRequests.contains(requestID)){
            System.out.println("processed at learner!");
        }else{
            slotNumber = ((Learn) msg).getSlotNumber();
            this.insertData(slotNumber,((Learn) msg).getValue());
            processedRequests.add(requestID);
        }

        System.out.printf("\n%s receive %s\n", myID, (Learn) msg);
        if(fail_send_to_client) {
            System.out.println("failed to respond to client");
        } else {
            this.respondToClient(msg);
        }

    }

    private void respondToClient(Message msg){
        System.out.println(myID+ " DATA "+ values);
        System.out.printf("server send %s => %s\n", new Response(myID, ((Learn) msg).getIndex()),Configuration.clientIDs.get(1) );
        network.sendMessage(Configuration.clientIDs.get(1), new Response(myID, ((Learn) msg).getIndex()));
    }

    private void insertData(int slotnumber, HashMap data){
        if(slotnumber < values.size()){
            values.set(slotnumber, data);
        }else{
            for(int j=values.size();j<slotnumber;j++){
                HashMap<Integer,Integer> ds = new HashMap<>();
                values.add(ds);
            }
            values.add(data);
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

    public void setFail_send_to_client() {
        fail_send_to_client = true;
    }

    public void unsetFail_send_to_client() {
        fail_send_to_client = false;
    }
}
