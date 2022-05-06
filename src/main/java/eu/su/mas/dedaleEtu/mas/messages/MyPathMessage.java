package eu.su.mas.dedaleEtu.mas.messages;

import jade.core.AID;
import jade.lang.acl.ACLMessage;

import java.io.IOException;
import java.io.Serializable;
import java.util.List;

/** Message de contenant le chemin lors d'un blocage
 * */
public class MyPathMessage extends ACLMessage {
    public MyPathMessage(AID sender, List<String> myPath , long timestamp) {
        super(ACLMessage.INFORM);
        this.setSender(sender);
        this.setProtocol("PATH");
        this.setPostTimeStamp(timestamp);
        try {
            this.setContentObject((Serializable) myPath);
        }catch(IOException e){
            this.setContent("Failed to serialize myPath");
        }
    }
}
