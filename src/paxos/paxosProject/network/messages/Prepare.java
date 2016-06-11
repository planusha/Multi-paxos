package paxosProject.network.messages;

import io.netty.buffer.ByteBuf;
import paxosProject.network.NodeIdentifier;

/**
 * Created by anusha on 4/16/16.
 */
public class Prepare extends Message {

    private long proposalNumber;

    protected Prepare(){}

    public Prepare(NodeIdentifier sender, long proposalNumber) {
        super(MSG_TYPE.Prepare, sender);
        this.proposalNumber = proposalNumber;
    }



    public long getProposalNumber(){
        return proposalNumber;
    }

    @Override
    public void serialize(ByteBuf buf){
        super.serialize(buf);
        buf.writeLong(proposalNumber);
    }

    @Override
    public void deserialize(ByteBuf buf){
        super.deserialize(buf);
        proposalNumber = buf.readLong();
    }

    @Override
    public String toString(){
        StringBuilder sb = new StringBuilder();
        sb.append("Prepare<src=").append(super.getSender())
                .append(" proposalNumber=").append(proposalNumber).append(">");

        return sb.toString();
    }
}
