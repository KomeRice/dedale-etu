package eu.su.mas.dedaleEtu.mas.behaviours.template;

import java.io.IOException;
import java.util.List;

import dataStructures.serializableGraph.SerializableSimpleGraph;

import eu.su.mas.dedale.mas.AbstractDedaleAgent;
import eu.su.mas.dedaleEtu.mas.knowledge.MapRepresentation;
import eu.su.mas.dedaleEtu.mas.knowledge.MapRepresentation.MapAttribute;

import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.SimpleBehaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.lang.acl.UnreadableException;

/**
 * The agent periodically share its map.
 * It blindly tries to send all its graph to its friend(s)  	
 * If it was written properly, this sharing action would NOT be in a ticker behaviour and only a subgraph would be shared.

 * @author hc
 *
 */
public class SendMessageBehaviour extends SimpleBehaviour{
	
	private MapRepresentation myMap;
	private List<String> receivers;
	/**
	 * The agent periodically share its map.
	 * It blindly tries to send all its graph to its friend(s)  	
	 * If it was written properly, this sharing action would NOT be in a ticker behaviour and only a subgraph would be shared.

	 * @param a the agent
	 * @param mymap (the map to share)
	 * @param receivers the list of agents to send the map to
	 */
	public SendMessageBehaviour(Agent a,MapRepresentation mymap, List<String> receivers) {
		super();
		this.myMap=mymap;
		this.receivers=receivers;	
	}

	/**
	 * 
	 */
	private static final long serialVersionUID = -568863390879327961L;


	@Override
	public void action() {
		System.out.println("Trying to broadcast");
		ACLMessage msg = new ACLMessage(ACLMessage.INFORM);
		msg.setProtocol("SHARE-TOPO");
		msg.setSender(this.myAgent.getAID());
		for (String agentName : receivers) {
			msg.addReceiver(new AID(agentName,AID.ISLOCALNAME));
		}
			
		SerializableSimpleGraph<String, MapAttribute> sg=this.myMap.getSerializableGraph();
		try {					
			msg.setContentObject(sg);
		} catch (IOException e) {
			e.printStackTrace();
		}
		((AbstractDedaleAgent)this.myAgent).sendMessage(msg);
		
	}

	@Override
	public boolean done() {
		return false;
	}
}