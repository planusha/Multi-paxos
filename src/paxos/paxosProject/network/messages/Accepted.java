package paxosProject.network.messages;

import io.netty.buffer.ByteBuf;
import paxosProject.network.NodeIdentifier;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * Created by anusha on 4/16/16.
 */
public class Accepted extends Message {
    // feel free to add other variables
    private int index;
    private boolean accepted;
    private HashMap<Integer,Integer> key_value_pair;
    private int hashmapsize;
    private int slotNumber;

    protected Accepted(){}

    public Accepted(NodeIdentifier sender, boolean accepted, int index, HashMap key_value_pair, int slotNumber) {
        super(MSG_TYPE.Accepted, sender);
        this.accepted = accepted;
        this.index = index;
        this.key_value_pair = key_value_pair;
        this.hashmapsize = key_value_pair.size();
        this.slotNumber = slotNumber;
    }

    public boolean getAcceptance(){
        return accepted;
    }

    public int getIndex(){
        return index;
    }

    public HashMap getValue(){
        return key_value_pair;
    }

    public int getSlotNumber(){
        return this.slotNumber;
    }


    @SuppressWarnings("Duplicates")
    @Override
    public void serialize(ByteBuf buf){
        super.serialize(buf);
        buf.writeBoolean(accepted);
        buf.writeInt(slotNumber);
        buf.writeInt(index);
        buf.writeInt(hashmapsize);
        Iterator<Map.Entry<Integer, Integer>> iterator = key_value_pair.entrySet().iterator();
        while(iterator.hasNext()){
            int key = iterator.next().getKey();
            int value = key_value_pair.get(key);
            buf.writeInt(key);
            buf.writeInt(value);
        }
    }

    @SuppressWarnings("Duplicates")
    @Override
    public void deserialize(ByteBuf buf){
        super.deserialize(buf);
        accepted = buf.readBoolean();
        slotNumber = buf.readInt();
        index = buf.readInt();
        hashmapsize = buf.readInt();
        int i =0;
        HashMap<Integer,Integer> hs = new HashMap<>();
        while(i<hashmapsize){
            int key = buf.readInt();
            int value = buf.readInt();
            hs.put(key,value);
            i++;
        }
        key_value_pair = hs;
    }

    @Override
    public String toString(){
        StringBuilder sb = new StringBuilder();
        sb.append("Accepted<src=").append(super.getSender())
                .append(" result=").append(accepted).append(">");

        return sb.toString();
    }
}
