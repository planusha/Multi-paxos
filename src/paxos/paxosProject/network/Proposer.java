package paxosProject.network;

import paxosProject.Configuration;
import paxosProject.network.messages.*;
import paxosProject.Configuration;


import javax.xml.soap.Node;
import java.nio.channels.ClosedChannelException;
import java.util.*;

/**
 * Created by anusha on 4/17/16.
 */
public class Proposer implements EventHandler {

    HashMap<Integer, Integer> requests_votes = new HashMap<>();
    HashSet<Integer> request_sent_to_learner = new HashSet<>();
    private int slotnumber =0;
    private boolean PrepareSent = false, UpdateSent = false;
    private long proposalNumber;
    private Boolean isReady = false, isLeader = false, LearnUpdateSent = false;
    private int prepareVotes =0, acceptorUpdateResponseVotes =0;
    private ArrayList<ArrayList<HashMap<HashMap<Integer,Integer>,Long>>> returnedAcceptorValues = new ArrayList<>();
    private int maxSlot =0;
    private ArrayList<HashMap<HashMap<Integer,Integer>,Long>> updatedValues;
    ArrayList<HashMap<HashMap<Integer,Integer>,Long>> ret = new ArrayList<>();
    private long leaderReply;
    private HashMap<Integer,Integer> clientRequestAcceptTracking = new HashMap<>();
    Boolean fail_leader = false;


    NodeIdentifier myID = null;
    Network network = null;
    public Proposer(NodeIdentifier myID){
        this.myID = myID;
        network = new NettyNetwork(myID, this);
        leaderReply = myID.getID();
    }


    @Override
    public void handleMessage(Message msg) {
        if(msg instanceof Accepted){
            this.handleAccepted(msg);
        }else if(msg instanceof Promise){
            this.handlePromise(msg);
        }else if(msg instanceof Put){
            this.handleClientReq(msg);
        }else if(msg instanceof Ping){
           this.handlePing(msg);
        }else if(msg instanceof UpdateResponse){
            this.handleAcceptorUpdate(msg);
        }

    }


    /**** Ping -> Ping ****/
    private void handlePing(Message msg){
        long pid = msg.getSenderID();
        if(pid<leaderReply){
            leaderReply = pid;
        }
    }

    /**** Accepted -> Learn  ****/
    private void handleAccepted(Message msg){

        System.out.printf("%s receive %s\n", myID, (Accepted) msg);
        if(((Accepted) msg).getAcceptance()){
            int requestID = ((Accepted) msg).getIndex();
            int votes = requests_votes.get(requestID);
            votes++;
            requests_votes.put(requestID,votes);
            if((votes> Configuration.MAX_FAILURES + 1) && !request_sent_to_learner.contains(requestID))
            {
                this.sendLearn(((Accepted) msg).getSlotNumber(), requestID, ((Accepted) msg).getValue());
                request_sent_to_learner.add(requestID);
                clientRequestAcceptTracking.put(requestID,((Accepted) msg).getSlotNumber());
            }
        }else{
            System.out.println("got a reject from acceptor in Accept phase");
            isLeader = false;
        }
    }

    /**** Put -> Accept - LeaderInfo ****/
    private void handleClientReq(Message msg){

        int requestID =((Put) msg).getIndex();
        if(isLeader && isReady)
        {
            this.requests_votes.put(((Put) msg).getIndex(),0);
            System.out.printf("%s receive %s\n", myID, msg);
            HashMap<Integer,Integer> value = ((Put) msg).getValue();
            if(clientRequestAcceptTracking.containsKey(requestID)){
                System.out.println("Recieved a request already accepted! -- resending Learn");
                this.sendLearn(clientRequestAcceptTracking.get(requestID),requestID,value);
            }else{
                this.sendAccept(msg, requestID);
            }

        }
        else if(!isLeader)
        {
            System.out.printf("%s receive %s\n", myID, msg);
            NodeIdentifier leader = this.getLeader();
            System.out.printf("server send %s => %s\n", new LeaderInfo(myID, leader,requestID, ((Put) msg).getValue()),msg.getSender());
            network.sendMessage(msg.getSender(), new LeaderInfo(myID, leader, requestID, ((Put) msg).getValue()));
        }
    }

    /**** Promise ****/
    @SuppressWarnings("Duplicates")
    private void handlePromise(Message msg){

        System.out.printf("%s receive %s\n", myID, msg);
        boolean promiseReply = ((Promise) msg).getPromise();
        if(promiseReply && isLeader){

            prepareVotes++;
            ret = ((Promise) msg).getValues();
            //System.out.println("PROMISE REPLY VALUES"+ ret+" size"+ ret.size());
            if(!((Promise) msg).isEmpty()){
                maxSlot = ret.size()> maxSlot ? ret.size() : maxSlot;
                returnedAcceptorValues.add(ret);
               // System.out.println("ret" + ret);
            }
            //TODO: merge with incremental approach
            if(prepareVotes > Configuration.MAX_FAILURES +1 && !UpdateSent){

                updatedValues = this.mergeValues(returnedAcceptorValues,maxSlot);
                //TODO: send updates to acceptors -- this.sendUpdatestoAcceptors(updatedValues);
                System.out.println("Proposer data" + updatedValues);
                slotnumber = maxSlot>=0? maxSlot: 0;

                this.sendUpdatesToAcceptors(updatedValues);
            }

        }else if (!promiseReply && !isReady) {
            System.out.println("got a reject from acceptor in Prepare phase");
            isLeader = false;
        }


    }

    @SuppressWarnings("Duplicates")
    private void handleAcceptorUpdate(Message msg){

        System.out.printf("%s receive %s\n", myID, msg);
        boolean acceptorUpdateReply = ((UpdateResponse) msg).getPromise();

        if(acceptorUpdateReply && isLeader){

            acceptorUpdateResponseVotes++;
            if(acceptorUpdateResponseVotes > Configuration.MAX_FAILURES +1 && !LearnUpdateSent){
                this.sendUpdatesToLearners(updatedValues);
                isReady = true;
            }

        }else if (!acceptorUpdateReply && !isReady) {
            System.out.println("got a reject from acceptor in Update phase");
            isLeader = false;
        }
    }

    private void sendAccept(Message msg, int requestID){

        for(Integer aid : Configuration.acceptorIDs.keySet()){
            NodeIdentifier acceptorID = Configuration.acceptorIDs.get(aid);
            System.out.printf("server send %s => %s\n", new Accept(myID, slotnumber, ((Put) msg).getValue(), proposalNumber, requestID), acceptorID);
            network.sendMessage(acceptorID, new Accept(myID, slotnumber, ((Put) msg).getValue(), proposalNumber, requestID));
        }
        slotnumber++;
    }

    private void sendLearn(int slotNum, int requestID, HashMap<Integer,Integer> value){

        for(Integer lid : Configuration.learnerIDs.keySet()){
            NodeIdentifier learnerID = Configuration.learnerIDs.get(lid);
            System.out.printf("server send %s => %s\n", new Learn(myID, slotNum, value , proposalNumber, requestID), learnerID);
            network.sendMessage(learnerID, new Learn(myID, slotNum, value, proposalNumber, requestID));
        }
    }

    private void sendPrepare(){
        System.out.println("NEW LEADER "+ myID);
        PrepareSent = true;
        for(Integer aid : Configuration.acceptorIDs.keySet()){
            NodeIdentifier acceptorID = Configuration.acceptorIDs.get(aid);
            System.out.printf("server send %s => %s\n", new Prepare(myID,proposalNumber), acceptorID);
            network.sendMessage(acceptorID, new Prepare(myID,proposalNumber));
        }
    }

    private void sendUpdatesToAcceptors(ArrayList<HashMap<HashMap<Integer,Integer>,Long>> data){

        for(Integer aid : Configuration.acceptorIDs.keySet()){
            NodeIdentifier acceptorID = Configuration.acceptorIDs.get(aid);
            System.out.printf("server send %s => %s\n", new Update(myID, data), acceptorID);
            network.sendMessage(acceptorID, new Update(myID, data));
        }
        UpdateSent = true;
    }

    private void sendUpdatesToLearners(ArrayList<HashMap<HashMap<Integer,Integer>,Long>> data){

        for(Integer lid : Configuration.learnerIDs.keySet()){
            NodeIdentifier learnerID = Configuration.learnerIDs.get(lid);
            System.out.printf("server send %s => %s\n", new Update(myID, data), learnerID);
            network.sendMessage(learnerID, new Update(myID, data));
        }
        LearnUpdateSent = true;
    }

    private ArrayList<HashMap<HashMap<Integer,Integer>,Long>> mergeValues(ArrayList<ArrayList<HashMap<HashMap<Integer,Integer>,Long>>> returnedAcceptorValues, int maxSlot){

        //UpdateSent = true;
        ArrayList<HashMap<HashMap<Integer,Integer>,Long>> updatedValues = new ArrayList<>();
        if(returnedAcceptorValues == null || returnedAcceptorValues.size() == 0){
            return updatedValues;
        }

        for(int i=0;i<maxSlot;i++){

            HashMap<HashMap<Integer,Integer>,Long> highestValue = new HashMap<HashMap<Integer,Integer>,Long>();
            long maxProposalValue=0;
            for(int j= 0; j < returnedAcceptorValues.size();j++){
                HashMap<HashMap<Integer,Integer>,Long> sd = returnedAcceptorValues.get(j).get(i);
                Iterator<Map.Entry<HashMap<Integer,Integer>, Long>> iterator1 = sd.entrySet().iterator();
                if(iterator1.hasNext()){
                    long curprop = iterator1.next().getValue();
                    if(curprop > maxProposalValue){
                        highestValue = sd;
                        maxProposalValue = curprop;
                    }
                }
            }
            updatedValues.add(highestValue);
        }

        return updatedValues;
    }

    @Override
    public void handleTimer() {

        if(this.isLeader() && !PrepareSent){
            proposalNumber = Configuration.epochNumber + 1;
            isLeader = true;
            this.sendPrepare();
        }

       // network.sendMessage(Configuration.epochNumber, new Ping());
    }

    private NodeIdentifier getLeader(){
        int id =  (int)(Configuration.epochNumber % Configuration.proposerIDs.size()) + 1;
        return Configuration.proposerIDs.get(id);
    }

    private boolean isLeader(){
        //System.out.println(" myid : " + this.myID + "  epoch " + Configuration.epochNumber);
        long id = (Configuration.epochNumber % Configuration.proposerIDs.size()) + 1;
        long pid = this.myID.getID();
        if(pid == id){
            return true;
        }
        return false;
    }

    public void startHeartBeat(){
        Runnable r = new Runnable() {
            @Override
            public void run() {
                try{
                    while(true) {
                            leaderReply = myID.getID();
                            for(Integer pid: Configuration.proposerIDs.keySet()){
                                NodeIdentifier propNode = Configuration.proposerIDs.get(pid);
                                if(pid != myID.getID()){
                                    if(((isLeader && !fail_leader) || !isLeader)){
                                       // System.out.println(isLeader +" <- isleader fail-leader -> "+ fail_leader);
                                        //System.out.printf("server send %s => %s\n", new Ping(myID), propNode);
                                        network.sendMessage(propNode, new Ping(myID));
                                    }
                                }
                            }
                            Thread.sleep(2* Configuration.pingTimeout);
                            if(!fail_leader){
                                if (leaderReply >= myID.getID() && !isLeader) {
                                    System.out.println(myID+"---RELECTION!");
                                    Configuration.electLeader();
                                }
                            }

                        }
                    }catch(Exception e){
                        System.out.println("Heartbeat Exception");
                    }
                }
        };
        new Thread(r).start();

    }

    @Override
    public void handleFailure(NodeIdentifier node, Throwable cause) {
        if (cause instanceof ClosedChannelException) {
            System.out.printf("%s handleFailure get %s\n", myID, cause);
        }
    }

    //TODO: merge with incremental approach.
    private void merge(ArrayList<HashMap<HashMap<Integer,Integer>, Long>> proposerValues, ArrayList<HashMap<HashMap<Integer,Integer>, Long>> newValues, boolean last){
        if(last){
            UpdateSent = true;
        }


        for(int i =0; i < newValues.size();i++ ){
            HashMap<HashMap<Integer,Integer>,Long> propData = proposerValues.get(i);
            HashMap<HashMap<Integer,Integer>,Long> newData = newValues.get(i);

        }
    }

    public void set_failLeader(){
        fail_leader = true;
    }
    public void unset_failLeader(){
        fail_leader = false;
    }



}
