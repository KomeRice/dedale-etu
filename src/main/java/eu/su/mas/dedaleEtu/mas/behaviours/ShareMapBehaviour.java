package eu.su.mas.dedaleEtu.mas.behaviours;

import java.io.IOException;
import java.time.Instant;
import java.util.List;

import dataStructures.serializableGraph.SerializableSimpleGraph;

import dataStructures.tuple.Couple;
import eu.su.mas.dedale.mas.AbstractDedaleAgent;
import eu.su.mas.dedaleEtu.mas.knowledge.AgentMeta;
import eu.su.mas.dedaleEtu.mas.knowledge.MapData;
import eu.su.mas.dedaleEtu.mas.knowledge.MapRepresentation;
import eu.su.mas.dedaleEtu.mas.knowledge.MapRepresentation.MapAttribute;

import eu.su.mas.dedaleEtu.mas.knowledge.Position;
import eu.su.mas.dedaleEtu.mas.messages.MetaMessage;
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
		String receiver = this.info.getLastReceiver();
		MapData mapData = this.info.getToSendMap(receiver);
		List<Position> interests = this.info.getInterests();
		MetaMessage msg = new MetaMessage(this.myAgent.getAID(),
				mapData,interests,
				Instant.now().toEpochMilli());
		msg.addReceiver(new AID(receiver,AID.ISLOCALNAME));

		((AbstractDedaleAgent)this.myAgent).sendMessage(msg);
		
		MessageTemplate msgTemplate=MessageTemplate.and(
				MessageTemplate.MatchProtocol("META"),
				MessageTemplate.MatchPerformative(ACLMessage.INFORM));
		ACLMessage msgReceived=this.myAgent.blockingReceive(msgTemplate, 500);
		if (msgReceived!=null) {
			Couple<MapData,List<Position>> sgreceived=null;
			try {
				sgreceived = (Couple<MapData,List<Position>>)msgReceived.getContentObject();
			} catch (UnreadableException e) {
				e.printStackTrace();
			}
			this.info.mergeMap(sgreceived.getLeft());
			this.info.mergeInterest(sgreceived.getRight());
			this.info.clearBlockedNodes();
			this.info.findTrajectory(((AbstractDedaleAgent) this.myAgent).getCurrentPosition());
		}
	}

	@Override
	public int onEnd() {
		return 727;
	}
}
