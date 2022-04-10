package eu.su.mas.dedaleEtu.mas.messages;

import eu.su.mas.dedaleEtu.mas.knowledge.AgentMeta;
import jade.core.AID;
import jade.lang.acl.ACLMessage;

import java.io.IOException;

public class MetaMessage extends ACLMessage {
    public MetaMessage(AID sender, AgentMeta info, long timestamp){
        super(ACLMessage.INFORM);
        this.setSender(sender);
        this.setProtocol("META");
        this.setPostTimeStamp(timestamp);
        try{
            this.setContentObject(info);
        }
        catch(IOException e){
            this.setContent("Failed to pack agent meta");
        }
    }
}
