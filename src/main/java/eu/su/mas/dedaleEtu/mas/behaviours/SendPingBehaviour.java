package eu.su.mas.dedaleEtu.mas.behaviours;

import java.text.SimpleDateFormat;
import java.time.Instant;
import java.util.Date;
import java.util.List;

import eu.su.mas.dedale.mas.AbstractDedaleAgent;
import eu.su.mas.dedaleEtu.mas.knowledge.AgentMeta;
import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.OneShotBehaviour;
import jade.core.behaviours.SimpleBehaviour;
import jade.lang.acl.ACLMessage;

public class SendPingBehaviour extends OneShotBehaviour {


	private AgentMeta info;
	
	public SendPingBehaviour (final Agent myagent,AgentMeta info) {
		super(myagent);
		this.info=info;
	}

	@Override
	public void action() {
		String myPosition=((AbstractDedaleAgent)this.myAgent).getCurrentPosition();
		ACLMessage msg=new ACLMessage(ACLMessage.INFORM);
		msg.setSender(this.myAgent.getAID());
		msg.setProtocol("PING");
		long now = Instant.now().toEpochMilli();
		if (myPosition!=""){
			//System.out.println("Agent "+this.myAgent.getLocalName()+ " is trying to reach its friends");
			msg.setContent("Hello World, I'm at "+myPosition);

			for (String agentName : info.getList_agentNames()) {
				msg.addReceiver(new AID(agentName,AID.ISLOCALNAME));
			}
			

			//Mandatory to use this method (it takes into account the environment to decide if someone is reachable or not)
			((AbstractDedaleAgent)this.myAgent).sendMessage(msg);
		}
	}

	@Override
	public int onEnd() {
		return 2; //2 -> pong
	}
}