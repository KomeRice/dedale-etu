package eu.su.mas.dedaleEtu.mas.messages;

import jade.core.AID;
import jade.lang.acl.ACLMessage;

public class BlockedMessage extends ACLMessage {
    public BlockedMessage(AID sender, String senderPosition, long timestamp){
        super(ACLMessage.INFORM);
        this.setSender(sender);
        this.setProtocol("BLOCKED");
        this.setPostTimeStamp(timestamp);
        this.setContent(senderPosition);
    }
}
