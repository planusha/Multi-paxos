package paxosProject.network.messages;

import io.netty.buffer.ByteBuf;
import paxosProject.network.NodeIdentifier;
import paxosProject.network.ServerData;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * Created by anusha on 4/20/16.
 */
public class Update extends Message {

    private ArrayList<HashMap<HashMap<Integer,Integer>,Long>> values = new ArrayList<>();
    HashMap<HashMap<Integer,Integer>,Long> HSvalue = new HashMap<>();
    HashMap<Integer,Integer> key_value_pair = new HashMap<>();
    private long values_size;

    protected Update(){}

    public Update(NodeIdentifier sender, ArrayList<HashMap<HashMap<Integer,Integer>,Long>> values) {
        super(MSG_TYPE.Update, sender);
        this.values = values;
        this.values_size = values!= null?values.size():0;
    }

    public boolean isEmpty(){
        if(values.size() == 0 || (values.size() == 1 && values.get(0).size() == 0))
            return true;
        return false;
    }

    public ArrayList getValues(){
        return  values;
    }

    @SuppressWarnings("Duplicates")
    @Override
    public void serialize(ByteBuf buf){
        super.serialize(buf);
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
        sb.append("Update<src=").append(super.getSender())
                .append(" Values = ").append(values)
                .append(" Size = ").append(values_size).append(">");

        return sb.toString();
    }
}


