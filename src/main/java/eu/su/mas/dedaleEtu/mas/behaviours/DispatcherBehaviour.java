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
            endCode = 727;
        }
    }

    @Override
    public int onEnd() {
        return endCode;
    }
}
