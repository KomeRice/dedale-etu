package eu.su.mas.dedaleEtu.mas.messages;

import eu.su.mas.dedaleEtu.mas.knowledge.AgentSpecs;
import jade.core.AID;
import jade.lang.acl.ACLMessage;

import java.io.IOException;
import java.io.Serializable;
import java.util.List;

public class CapacityMessage extends ACLMessage {
    public CapacityMessage(AID sender, AgentSpecs agentSpecs, long timestamp) {
        super(ACLMessage.INFORM);
        this.setSender(sender);
        this.setProtocol("SPECS");
        this.setPostTimeStamp(timestamp);
        try {
            this.setContentObject(agentSpecs);
        }catch(IOException e){
            this.setContent("Failed to serialize myPath");
        }
    }
}