package eu.su.mas.dedaleEtu.mas.behaviours;

import java.io.IOException;

import dataStructures.serializableGraph.SerializableSimpleGraph;

import eu.su.mas.dedale.mas.AbstractDedaleAgent;
import eu.su.mas.dedaleEtu.mas.knowledge.AgentMeta;
import eu.su.mas.dedaleEtu.mas.knowledge.MapData;
import eu.su.mas.dedaleEtu.mas.knowledge.MapRepresentation;
import eu.su.mas.dedaleEtu.mas.knowledge.MapRepresentation.MapAttribute;

import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.OneShotBehaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.lang.acl.UnreadableException;


public class ShareMapBehaviour extends OneShotBehaviour {
	
	private AgentMeta info;
	//todo: update doc

	public ShareMapBehaviour(Agent a, AgentMeta info) {
		super(a);
		this.info = info;
	}

	private static final long serialVersionUID = -568863390879327961L;

	@Override
	public void action() {
		ACLMessage msg = new ACLMessage(ACLMessage.INFORM);
		msg.setProtocol("SHARE-TOPO");
		msg.setSender(this.myAgent.getAID());
		String receiver = this.info.getLastReceiver();

		msg.addReceiver(new AID(receiver,AID.ISLOCALNAME));
		MapData mapData = this.info.getToSendMap(receiver);
		try {					
			msg.setContentObject(mapData);
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
			MapData sgreceived=null;
			try {
				sgreceived = (MapData)msgReceived.getContentObject();
			} catch (UnreadableException e) {
				e.printStackTrace();
			}
			this.info.mergeMap(sgreceived);
			System.out.println("Received map");

		}
	}

	@Override
	public int onEnd() {
		return 727;
	}
}
