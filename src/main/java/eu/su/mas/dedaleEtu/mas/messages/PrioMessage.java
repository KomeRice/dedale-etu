package eu.su.mas.dedaleEtu.mas.messages;

import jade.core.AID;
import jade.lang.acl.ACLMessage;

public class PrioMessage extends ACLMessage {
    public PrioMessage(AID sender, int myPriority , long timestamp){
        super(ACLMessage.INFORM);
        this.setSender(sender);
        this.setProtocol("PRIO");
        this.setPostTimeStamp(timestamp);
        this.setContent(String.valueOf(myPriority));
    }
}
