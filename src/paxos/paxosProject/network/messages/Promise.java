package paxosProject.network.messages;

import io.netty.buffer.ByteBuf;
import paxosProject.network.NodeIdentifier;
import paxosProject.network.ServerData;

import java.util.*;

/**
 * Created by anusha on 4/16/16.
 */
public class Promise extends Message {
    private ArrayList<HashMap<HashMap<Integer,Integer>, Long>> values = new ArrayList<>();
    HashMap<HashMap<Integer,Integer>,Long> HSvalue = new HashMap<>();
    HashMap<Integer,Integer> key_value_pair = new HashMap<>();
    private boolean isPromising;
    private long values_size;

    protected Promise(){}

    public Promise(NodeIdentifier sender, boolean promise, ArrayList<HashMap<HashMap<Integer,Integer>, Long>> values) {
        super(MSG_TYPE.Promise, sender);
        this.isPromising = promise;
        this.values = values;
        this.values_size = values!= null?values.size(): 0;
    }


    public boolean getPromise(){
        return isPromising;
    }

    public ArrayList<HashMap<HashMap<Integer,Integer>, Long>> getValues(){
        return  values;
    }

    public boolean isEmpty(){
        if(values.size() == 0 || (values.size() == 1 && values.get(0).size() == 0))
            return true;
        return false;
    }

    @SuppressWarnings("Duplicates")
    @Override
    public void serialize(ByteBuf buf){
        super.serialize(buf);
        buf.writeBoolean(isPromising);
        buf.writeLong(values_size);
        for(int j=0;j<values_size;j++){
            HSvalue = values.get(j);
            for(HashMap<Integer,Integer> hs : HSvalue.keySet()){
                for(int key: hs.keySet()){
                    buf.writeInt(key);
                    buf.writeInt(hs.get(key));
                }
                buf.writeLong(HSvalue.get(hs));
            }
        }
    }

    @SuppressWarnings("Duplicates")
    @Override
    public void deserialize(ByteBuf buf){
        super.deserialize(buf);
        isPromising = buf.readBoolean();
        values_size = buf.readLong();
        if(buf.readableBytes() != 0){
            for(int j =0;j<values_size;j++){
                HSvalue = new HashMap<>();
                key_value_pair = new HashMap<>();
                int key = buf.readInt();
                int value = buf.readInt();
                key_value_pair.put(key,value);
                long proposalNum = buf.readLong();
                HSvalue.put(key_value_pair,proposalNum);
                values.add(HSvalue);
            }
        }
    }

    @Override
    public String toString(){
        StringBuilder sb = new StringBuilder();
        sb.append("Promise<src=").append(super.getSender())
                .append(" isPromising=").append(isPromising)
                .append(" Values = ").append(values)
                .append(" Size = ").append(values_size).append(">");

        return sb.toString();
    }
}
