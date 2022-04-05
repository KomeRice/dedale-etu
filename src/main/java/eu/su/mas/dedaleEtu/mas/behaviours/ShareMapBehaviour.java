package eu.su.mas.dedaleEtu.mas.behaviours;

import java.io.IOException;

import dataStructures.serializableGraph.SerializableSimpleGraph;

import eu.su.mas.dedale.mas.AbstractDedaleAgent;
import eu.su.mas.dedaleEtu.mas.knowledge.AgentMeta;
import eu.su.mas.dedaleEtu.mas.knowledge.MapRepresentation.MapAttribute;

import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.OneShotBehaviour;
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
public class ShareMapBehaviour extends OneShotBehaviour {
	
	private AgentMeta info;
	//todo: update doc
	/**
	 * The agent periodically share its map.
	 * It blindly tries to send all its graph to its friend(s)  	
	 * If it was written properly, this sharing action would NOT be in a ticker behaviour and only a subgraph would be shared.

	 * @param a the agent
	 */
	public ShareMapBehaviour(Agent a, AgentMeta info) {
		super(a);
		this.info = info;
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
		for (String agentName : info.getListReceiverAgents()) {
			msg.addReceiver(new AID(agentName,AID.ISLOCALNAME));
		}
			
		SerializableSimpleGraph<String, MapAttribute> sg=this.info.getMyMap().getSerializableGraph();
		try {					
			msg.setContentObject(sg);
		} catch (IOException e) {
			e.printStackTrace();
		}
		((AbstractDedaleAgent)this.myAgent).sendMessage(msg);
		
		MessageTemplate msgTemplate=MessageTemplate.and(
				MessageTemplate.MatchProtocol("SHARE-TOPO"),
				MessageTemplate.MatchPerformative(ACLMessage.INFORM));
		ACLMessage msgReceived=this.myAgent.blockingReceive(msgTemplate, 500);
		if (msgReceived!=null) {
			System.out.println("Trying to read");
			SerializableSimpleGraph<String, MapAttribute> sgreceived=null;
			try {
				sgreceived = (SerializableSimpleGraph<String, MapAttribute>)msgReceived.getContentObject();
			} catch (UnreadableException e) {
				e.printStackTrace();
			}
			this.info.getMyMap().mergeMap(sgreceived);
			System.out.println("Received map");

		}
	}

	@Override
	public int onEnd() {
		return 727;
	}
}
