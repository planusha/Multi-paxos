package paxosProject.network.messages;

import io.netty.buffer.ByteBuf;
import paxosProject.network.NodeIdentifier;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by anusha on 4/28/16.
 */
public class UpdateResponse extends Message{
    private boolean isPromising;


    protected UpdateResponse(){}

    public UpdateResponse(NodeIdentifier sender, boolean promise) {
        super(Message.MSG_TYPE.UpdateResponse, sender);
        this.isPromising = promise;
    }


    public boolean getPromise(){
        return isPromising;
    }


    @SuppressWarnings("Duplicates")
    @Override
    public void serialize(ByteBuf buf){
        super.serialize(buf);
        buf.writeBoolean(isPromising);

    }

    @SuppressWarnings("Duplicates")
    @Override
    public void deserialize(ByteBuf buf){
        super.deserialize(buf);
        isPromising = buf.readBoolean();
    }

    @Override
    public String toString(){
        StringBuilder sb = new StringBuilder();
        sb.append("UpdateResponse <src=").append(super.getSender())
                .append(" isPromising=").append(isPromising)
                .append(">");

        return sb.toString();
    }
}
