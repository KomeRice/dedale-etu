package eu.su.mas.dedaleEtu.mas.behaviours;

import java.time.Instant;
import java.util.Objects;

import eu.su.mas.dedale.mas.AbstractDedaleAgent;
import eu.su.mas.dedaleEtu.mas.knowledge.AgentMeta;
import eu.su.mas.dedaleEtu.mas.messages.PingMessage;
import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.OneShotBehaviour;

/**Comportement d'envoie de Ping */
public class SendPingBehaviour extends OneShotBehaviour {


	private AgentMeta info;
	
	public SendPingBehaviour (final Agent myagent,AgentMeta info) {
		super(myagent);
		this.info=info;
	}

	@Override
	public void action() {
		String myPosition = ((AbstractDedaleAgent)this.myAgent).getCurrentPosition();

		if (!Objects.equals(myPosition, "")){
			/*Creation de message*/
			PingMessage msg = new PingMessage(this.myAgent.getAID(),
					myPosition,
					Instant.now().toEpochMilli());
			//System.out.println("Agent "+this.myAgent.getLocalName()+ " is trying to reach its friends");
			msg.setContent(myPosition);

			for (String agentName : info.getListReceiverAgents()) {
				msg.addReceiver(new AID(agentName,AID.ISLOCALNAME));
			}
			
			/*Envoie*/
			((AbstractDedaleAgent)this.myAgent).sendMessage(msg);
		}
	}

	@Override
	public int onEnd() {
		return 2; //2 -> dispatcher
	}
}