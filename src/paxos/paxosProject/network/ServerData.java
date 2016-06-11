package paxosProject.network;


import java.util.HashMap;

/**
 * Created by anusha on 4/19/16.
 */
public class ServerData {

    private long proposalNumber;
    public HashMap<Integer, Integer> key_value_pairs;

    protected ServerData(){}

    public ServerData(long proposalNumber, HashMap hs){
        this.proposalNumber = proposalNumber;
        this.key_value_pairs = hs;
    }



    public long getProposalNumber(){
        if(proposalNumber >=0){
            return proposalNumber;
        }
        return Integer.MIN_VALUE;
    }

    @Override
    public String toString(){
        StringBuilder sb = new StringBuilder();
        sb.append("ServerData--Proposal number=").append(proposalNumber);
        return sb.toString();
    }
}
