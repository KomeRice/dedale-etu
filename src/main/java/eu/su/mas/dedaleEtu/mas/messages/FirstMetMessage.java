package eu.su.mas.dedaleEtu.mas.messages;

import dataStructures.tuple.Couple;
import eu.su.mas.dedaleEtu.mas.knowledge.AgentSpecs;
import jade.core.AID;
import jade.lang.acl.ACLMessage;

import java.io.IOException;
import java.util.Hashtable;

/** Message d'envoie de characteristique lors de la premiere rencontre
 * */
public class FirstMetMessage extends ACLMessage {
    public FirstMetMessage(AID sender, String pos, Hashtable<String, AgentSpecs> agentSpecs, long timestamp) {
        super(ACLMessage.INFORM);
        this.setSender(sender);
        this.setProtocol("SPECS");
        this.setPostTimeStamp(timestamp);
        try {
            this.setContentObject(new Couple<>(pos,agentSpecs));
        }catch(IOException e){
            this.setContent("Failed to serialize");
        }
    }
}