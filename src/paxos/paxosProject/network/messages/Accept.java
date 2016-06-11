package paxosProject.network.messages;

import io.netty.buffer.ByteBuf;
import paxosProject.network.NodeIdentifier;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * Created by anusha on 4/16/16.
 */
public class Accept extends Message {

    private int slotNumber;
    private HashMap<Integer,Integer> key_value_pair;
    private long proposalNumber;
    private int hashmapsize;
    private int index;

    protected Accept(){}

    public Accept(NodeIdentifier sender, int slotNumber, HashMap key_value_pair, long proposalNumber, int index) {
        super(MSG_TYPE.Accept, sender);
        this.slotNumber = slotNumber;
        this.key_value_pair = key_value_pair;
        this.proposalNumber = proposalNumber;
        this.hashmapsize = key_value_pair.size();
        this.index = index;
    }

    public long getProposalNumber(){
        return this.proposalNumber;
    }

    public int getSlotNumber(){
        return this.slotNumber;
    }

    public HashMap getValue(){ return this.key_value_pair; }

    public int getIndex() {
        return index;
    }

    @SuppressWarnings("Duplicates")
    @Override
    public void serialize(ByteBuf buf){
        super.serialize(buf);
        buf.writeInt(index);
        buf.writeInt(slotNumber);
        buf.writeLong(proposalNumber);
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
        slotNumber = buf.readInt();
        proposalNumber = buf.readLong();
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
        sb.append("Accept<src=").append(super.getSender())
                .append(" slotnumber=").append(slotNumber)
                .append(" key_value: ").append(key_value_pair)
                .append(" proposal number =").append(proposalNumber).append(">");

        return sb.toString();
    }
}
