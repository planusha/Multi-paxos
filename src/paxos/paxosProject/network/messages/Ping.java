package paxosProject.network.messages;

import io.netty.buffer.ByteBuf;
import paxosProject.network.NodeIdentifier;

/**
 * Created by anusha on 4/19/16.
 */
public class Ping extends Message {

    protected Ping(){}

    public Ping(NodeIdentifier sender) {
        super(MSG_TYPE.Ping, sender);
    }

    @Override
    public void serialize(ByteBuf buf){
        super.serialize(buf);
    }

    @Override
    public void deserialize(ByteBuf buf){
        super.deserialize(buf);
    }

    @Override
    public String toString(){
        StringBuilder sb = new StringBuilder();
        sb.append("HEARTBEAT<src=").append(super.getSender());

        return sb.toString();
    }
}
