package eu.su.mas.dedaleEtu.mas.messages;

import dataStructures.tuple.Couple;
import eu.su.mas.dedaleEtu.mas.knowledge.AgentMeta;
import eu.su.mas.dedaleEtu.mas.knowledge.MapData;
import eu.su.mas.dedaleEtu.mas.knowledge.Position;
import jade.core.AID;
import jade.lang.acl.ACLMessage;

import java.io.IOException;
import java.util.List;

/** Message de partage de carte et des points de ressources
 * */
public class MetaMessage extends ACLMessage {
    public MetaMessage(AID sender, MapData mapData, List<Position> interest, long timestamp){
        super(ACLMessage.INFORM);
        this.setSender(sender);
        this.setProtocol("META");
        this.setPostTimeStamp(timestamp);
        try{
            this.setContentObject(new Couple<>(mapData,interest));
        }
        catch(IOException e){
            this.setContent("Failed to pack agent meta");
        }
    }
}
