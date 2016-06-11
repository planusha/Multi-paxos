package paxosProject.network.messages;

import io.netty.buffer.ByteBuf;
import paxosProject.network.NodeIdentifier;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * Created by anusha on 4/23/16.
 */
public class LeaderInfo extends Message {
    private NodeIdentifier leader;
    private int index;
    private HashMap<Integer,Integer> key_value_pair;
    private int hashmapsize;

    protected LeaderInfo(){}

    public LeaderInfo(NodeIdentifier sender, NodeIdentifier leader, int index, HashMap<Integer,Integer> hs) {
        super(MSG_TYPE.LeaderInfo, sender);
        this.leader = leader;
        this.key_value_pair = hs;
        this.index = index;
        this.hashmapsize = key_value_pair.size();
    }

    public NodeIdentifier getLeader(){
        return leader;
    }

    public HashMap getValue(){
        return key_value_pair;
    }

    public int getIndex(){
        return index;
    }

    @SuppressWarnings("Duplicates")
    @Override
    public void serialize(ByteBuf buf){
        super.serialize(buf);
        buf.writeInt(leader.hashCode());
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
        leader = new NodeIdentifier(buf.readInt());
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
        sb.append("LeaderInfo<src=").append(super.getSender())
                .append(" Leader ID =").append(leader)
                .append("index = ").append(index).append(">");

        return sb.toString();
    }
}
