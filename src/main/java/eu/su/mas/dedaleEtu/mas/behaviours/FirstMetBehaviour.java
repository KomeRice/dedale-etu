package eu.su.mas.dedaleEtu.mas.behaviours;

import dataStructures.tuple.Couple;
import eu.su.mas.dedale.mas.AbstractDedaleAgent;
import eu.su.mas.dedaleEtu.mas.knowledge.AgentMeta;
import eu.su.mas.dedaleEtu.mas.knowledge.AgentSpecs;
import eu.su.mas.dedaleEtu.mas.messages.FirstMetMessage;
import jade.core.AID;
import jade.core.behaviours.OneShotBehaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.lang.acl.UnreadableException;

import java.time.Instant;
import java.util.Objects;

/**Comportement de premiere rencontre */
public class FirstMetBehaviour extends OneShotBehaviour {
    private AgentMeta info;

    public FirstMetBehaviour(AbstractDedaleAgent a, AgentMeta info){
        super(a);
        this.info = info;
    }

    @Override
    public void action() {
        /*Creation de message*/
        String receiver = this.info.getLastReceiver();
        String pos = ((AbstractDedaleAgent)this.myAgent).getCurrentPosition();
        if(!Objects.equals(info.getRdvPoint(), "")){
            pos = info.getRdvPoint();
        }
        AgentSpecs agentSpecs = info.getMySpecs();
        info.addSpecs(myAgent.getLocalName(),agentSpecs);
        FirstMetMessage toSend = new FirstMetMessage(this.myAgent.getAID(),
                pos,agentSpecs,
                Instant.now().toEpochMilli());
        toSend.addReceiver(new AID(receiver,AID.ISLOCALNAME));
        /*envoie*/
        ((AbstractDedaleAgent)this.myAgent).sendMessage(toSend);

        /*Reception*/
        MessageTemplate msgTemplate=MessageTemplate.and(
                MessageTemplate.MatchProtocol("SPECS"),
                MessageTemplate.MatchPerformative(ACLMessage.INFORM));
        ACLMessage msgReceived=this.myAgent.blockingReceive(msgTemplate, 800);
        if (msgReceived!=null) {
            Couple<String, AgentSpecs> sgreceived = null;
            try {
                sgreceived = (Couple<String, AgentSpecs>) msgReceived.getContentObject();
            } catch (UnreadableException e) {
                e.printStackTrace();
            }
            this.info.setRdvPoint(sgreceived.getLeft());
            this.info.addSpecs(msgReceived.getSender().getLocalName(), sgreceived.getRight());
        }
    }

    @Override
    public int onEnd() {
        if(info.getBlockStep()>0){
            return 2;//2-> blocked
        }
        return 1;//-> sharing
    }
}
