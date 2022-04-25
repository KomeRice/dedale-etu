package eu.su.mas.dedaleEtu.mas.behaviours;

import eu.su.mas.dedale.mas.AbstractDedaleAgent;
import eu.su.mas.dedaleEtu.mas.knowledge.AgentMeta;
import jade.core.behaviours.OneShotBehaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;


public class DispatcherBehaviour extends OneShotBehaviour {
    private AgentMeta info;
    private int endCode = 3;

    public DispatcherBehaviour(AbstractDedaleAgent a, AgentMeta info){
        super(a);
        this.info = info;
    }

    @Override
    public void action() {
        MessageTemplate msgTemplate = MessageTemplate.MatchProtocol("PING");
        ACLMessage msgReceived = this.myAgent.receive(msgTemplate);
        if(msgReceived != null){
            // System.out.println("RECEIVED A PING " + msgReceived.getContent());
            this.info.flagBlockedNode(msgReceived);
            String lastReceiver = msgReceived.getSender().getLocalName();
            this.info.setLastReceiver(lastReceiver);
            if (info.didMet(lastReceiver)){
                endCode = 727; // did met
            }else{
                endCode = 10; // not met yet
            }
        }
        else{
            this.info.clearBlockedNodes();
        }

        MessageTemplate msgTemplate2 = MessageTemplate.MatchProtocol("BLOCKED");
        msgReceived = this.myAgent.receive(msgTemplate2);
        if (msgReceived != null){
            String lastReceiver = msgReceived.getSender().getLocalName();
            this.info.setLastReceiver(lastReceiver);
            endCode = 9;//BLOCKED
        }

    }

    @Override
    public int onEnd() {
        return endCode;
    }
}
