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
        if(info.isExploEnded()){
            endCode = 4;
        }
        if (info.isFinished()){
            endCode = -1;
        }


        MessageTemplate msgTemplate = MessageTemplate.MatchProtocol("PING");
        ACLMessage msgReceived = this.myAgent.receive(msgTemplate);
        if(msgReceived != null){
            // System.out.println("RECEIVED A PING " + msgReceived.getContent());
            this.info.clearBlockedNodes();
            this.info.flagBlockedNode(msgReceived);
            String lastReceiver = msgReceived.getSender().getLocalName();
            this.info.setLastReceiver(lastReceiver);

            if (info.didMet(lastReceiver)){
                endCode = 727; // did met -> share map
            }else{
                info.addMet(lastReceiver);
                endCode = 10; // not met yet
            }
        }

        MessageTemplate msgTemplate2 = MessageTemplate.MatchProtocol("BLOCKED");
        msgReceived = this.myAgent.receive(msgTemplate2);
        if (msgReceived != null){
            String lastReceiver = msgReceived.getSender().getLocalName();
            this.info.setLastReceiver(lastReceiver);
            info.setBlockStep(3);
            if(info.didMet(lastReceiver)){
                endCode = 9;//BLOCKED
            }else {
                info.addMet(lastReceiver);
                endCode = 10;
            }

        }

    }

    @Override
    public int onEnd() {
        return endCode;
    }
}
