package eu.su.mas.dedaleEtu.mas.messages;

import jade.core.AID;
import jade.lang.acl.ACLMessage;

/** Message de validation de message recus
 * */
public class AckMessage extends ACLMessage {
    public AckMessage(AID sender, long timestamp) {
        super(ACLMessage.INFORM);
        this.setSender(sender);
        this.setProtocol("ACK");
        this.setPostTimeStamp(timestamp);
    }
}
