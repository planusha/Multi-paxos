package paxosProject.network.messages;

import io.netty.buffer.ByteBuf;
import paxosProject.network.NodeIdentifier;

/**
 * Created by anusha on 4/17/16.
 */
public class Response extends Message {
    private int index;

    protected Response(){}

    public Response(NodeIdentifier sender, int index) {
        super(MSG_TYPE.Response, sender);
        this.index = index;
    }

    public int getIndex(){
        return index;
    }

    @Override
    public void serialize(ByteBuf buf){
        super.serialize(buf);
        buf.writeInt(index);
    }

    @Override
    public void deserialize(ByteBuf buf){
        super.deserialize(buf);
        index = buf.readInt();
    }

    @Override
    public String toString(){
        StringBuilder sb = new StringBuilder();
        sb.append("Response<src=").append(super.getSender())
                .append(" index=").append(index).append(">");

        return sb.toString();
    }
}
