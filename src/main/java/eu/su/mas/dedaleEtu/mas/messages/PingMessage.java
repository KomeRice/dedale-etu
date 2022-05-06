package eu.su.mas.dedaleEtu.mas.messages;

import jade.core.AID;
import jade.lang.acl.ACLMessage;

/** Message de Ping
 * */
public class PingMessage extends ACLMessage {
    public PingMessage(AID sender, String senderPosition, long timestamp){
        super(ACLMessage.INFORM);
        this.setSender(sender);
        this.setProtocol("PING");
        this.setPostTimeStamp(timestamp);
        this.setContent(senderPosition);
    }
}
