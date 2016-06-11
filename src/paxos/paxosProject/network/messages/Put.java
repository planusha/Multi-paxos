package paxosProject.network.messages;

import io.netty.buffer.ByteBuf;
import paxosProject.network.NodeIdentifier;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * Created by anusha on 4/17/16.
 */
public class Put extends Message {

    private HashMap<Integer,Integer> key_value_pair;
    private int hashmapsize;
    private int index;

    protected Put(){}

    public Put(NodeIdentifier sender,int index, HashMap<Integer,Integer> hs) {
        super(MSG_TYPE.Put, sender);
        this.key_value_pair = hs;
        this.index = index;
        this.hashmapsize = key_value_pair.size();
    }


    public int getIndex() {
        return index;
    }

    public HashMap getValue(){
        return key_value_pair;
    }

    @SuppressWarnings("Duplicates")
    @Override
    public void serialize(ByteBuf buf){
        super.serialize(buf);
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
        index = buf.readInt();
        hashmapsize = buf.readInt();
        int i =0;
        HashMap<Integer,Integer> hs = new HashMap<>();
        while(i<hashmapsize){
            int key = buf.readInt();
            int value = buf.readInt();
           // System.out.println("value desel :"+value);
            hs.put(key,value);
            i++;
        }
        key_value_pair = hs;
    }

    @Override
    public String toString(){
        StringBuilder sb = new StringBuilder();
        sb.append("Put<src=").append(super.getSender())
                .append(" index= ").append(index)
                .append(" Key_value=").append(key_value_pair).append(">");

        return sb.toString();
    }
}
