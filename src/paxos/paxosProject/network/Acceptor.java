package paxosProject.network;

import paxosProject.Configuration;
import paxosProject.network.messages.*;

import java.nio.channels.ClosedChannelException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.PrimitiveIterator;

import paxosProject.network.ServerData;

/**
 * Created by anusha on 4/17/16.
 */
public class Acceptor implements EventHandler {

    //private int slotNumber;
    private int slotNumber;
    private long curProposalNum,prevProposalNum;
    private ArrayList<HashMap<HashMap<Integer,Integer>,Long>> values = new ArrayList<>();
    private HashMap<HashMap<Integer,Integer>, Long> key_value;
    private HashMap<Integer, Integer> key;
    private Long value;

    NodeIdentifier myID = null;
    Network network = null;
    public Acceptor(NodeIdentifier myID){
        this.myID = myID;
        network = new NettyNetwork(myID, this);
    }

    @Override
    public void handleMessage(Message msg) {

        if(msg instanceof Prepare){
            System.out.printf("%s receive %s\n", myID, (Prepare) msg);
            this.handlePrepare(msg);
        } else if (msg instanceof Accept) {
            this.handleAccept(msg);
        } else if(msg instanceof Update){

            this.handleUpdate(msg);
        }
    }

    private void handlePrepare(Message msg){

        curProposalNum = ((Prepare) msg).getProposalNumber();
        prevProposalNum = prevProposalNum>=0? prevProposalNum:curProposalNum;
        if(curProposalNum>prevProposalNum){
            prevProposalNum = curProposalNum;
            System.out.printf("server send %s => %s\n", new Promise(myID, true, values), msg.getSender());
            network.sendMessage(msg.getSender(), new Promise(myID, true, values));
        }else{
            System.out.printf("server send %s => %s\n", new Promise(myID, true, null), msg.getSender());
            network.sendMessage(msg.getSender(), new Promise(myID, false, null));
        }

    }

    private void handleUpdate(Message msg){

        System.out.printf("%s receive %s\n", myID, (Update) msg);
        values  = ((Update) msg).getValues();
        System.out.printf("server send %s => %s\n", new UpdateResponse(myID,true), msg.getSender());
        network.sendMessage(msg.getSender(), new UpdateResponse(myID, true));
    }

    private void handleAccept(Message msg){

        slotNumber = ((Accept) msg).getSlotNumber();
        curProposalNum = ((Accept) msg).getProposalNumber();
        key_value = this.getValueAtSlot(slotNumber);
        System.out.printf("%s  receive %s\n", myID, msg);

        /*---checking the proposal number of existing value and comparing with new proposal number---*/

        if(key_value != null){
            for(HashMap hs : key_value.keySet()){
                prevProposalNum = key_value.get(hs);
            }
        }//else{
//            prevProposalNum =0;
//        }

        System.out.println("Previous proposal number = " +prevProposalNum+ " Current proposal number = " + curProposalNum);
        if(prevProposalNum <= curProposalNum )
        {
            prevProposalNum = curProposalNum;
            this.insertData(slotNumber,curProposalNum, ((Accept) msg).getValue());
            System.out.println("Values at acceptor "+ myID+ " value =" + values);
            network.sendMessage(msg.getSender(), new Accepted(myID, true, ((Accept) msg).getIndex(),((Accept) msg).getValue(), slotNumber));
        }
        else
        {
            network.sendMessage(msg.getSender(), new Accepted(myID, false, ((Accept) msg).getIndex(), ((Accept) msg).getValue(), slotNumber));
        }
    }


    private void insertData( int slotnumber, long curProposalNum, HashMap<Integer,Integer> hs){
        HashMap<HashMap<Integer,Integer>,Long> hhs = new HashMap<>();

        if(slotnumber < values.size()){
            hhs.put(hs,curProposalNum);
            values.set(slotnumber, hhs);
        }else{
            if (values.size()> 0) {
                for(int j=values.size();j<slotnumber;j++){
                    values.add(hhs);
                }
            }else{
                for(int j=0;j<slotnumber;j++){
                    values.add(hhs);
                }
            }
            hhs.put(hs,curProposalNum);
            values.add(hhs);
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

    private HashMap<HashMap<Integer,Integer>,Long> getValueAtSlot(int slotNumber){
        if(slotNumber < values.size()){
            return values.get(slotNumber);
        }
        return null;
    }


}
