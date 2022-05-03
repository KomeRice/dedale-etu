package eu.su.mas.dedaleEtu.mas.behaviours;

import dataStructures.tuple.Couple;
import eu.su.mas.dedale.env.Observation;
import eu.su.mas.dedale.mas.AbstractDedaleAgent;
import eu.su.mas.dedaleEtu.mas.knowledge.AgentMeta;
import eu.su.mas.dedaleEtu.mas.knowledge.AgentSpecs;
import eu.su.mas.dedaleEtu.mas.knowledge.MapData;
import eu.su.mas.dedaleEtu.mas.knowledge.Position;
import eu.su.mas.dedaleEtu.mas.messages.BlockedMessage;
import eu.su.mas.dedaleEtu.mas.messages.MyPathMessage;
import jade.core.AID;
import jade.core.behaviours.OneShotBehaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.lang.acl.UnreadableException;

import java.time.Instant;
import java.util.Hashtable;
import java.util.List;

public class BlockedBehaviour extends OneShotBehaviour {
    // step ==0 -> find another route
    //step == 1 -> broadcast im blocked
    //stp 2 -> send prio message and receive it
    //step 3 if my prio is low -> wait for message else send my path
    // step 4 follow the path until you can unlock then wait
    // if thre is a time out continue exploring/collecting

    private AgentMeta info;
    private int state;

    public BlockedBehaviour(AbstractDedaleAgent a, AgentMeta info) {
        super(a);
        this.info = info;
    }

    @Override
    public void action() {
        switch (info.getBlockStep()){
            case 1:
                //find another route
            case 2:
                String myPosition = ((AbstractDedaleAgent)myAgent).getCurrentPosition();
                BlockedMessage msg = new BlockedMessage(myAgent.getAID(),myPosition, Instant.now().toEpochMilli());
                for (String agentName : info.getListReceiverAgents()) {
                    msg.addReceiver(new AID(agentName,AID.ISLOCALNAME));
                }
                ((AbstractDedaleAgent)this.myAgent).sendMessage(msg);
                state = 1; //go to dispatcher
                break;
            case 3:
                String receiver = this.info.getLastReceiver();
                Hashtable<String, AgentSpecs> specs = info.getAgentSpecsHashtable();
                if(info.getMySpecs().getPrio()>specs.get(receiver).getPrio()){
                    List<String> currentTrajectory =  info.getCurrentTrajectory();
                    MyPathMessage msg2 = new MyPathMessage(myAgent.getAID(),currentTrajectory, Instant.now().toEpochMilli());
                    msg2.addReceiver(new AID(receiver,AID.ISLOCALNAME));
                    ((AbstractDedaleAgent)this.myAgent).sendMessage(msg2);
                }else{
                    MessageTemplate msgTemplate=MessageTemplate.and(
                            MessageTemplate.MatchProtocol("PATH"),
                            MessageTemplate.MatchPerformative(ACLMessage.INFORM));
                    ACLMessage msgReceived=this.myAgent.blockingReceive(msgTemplate, 500);
                    if (msgReceived!=null) {
                        List<String> sgreceived = null;
                        try {
                            sgreceived = (List<String>) msgReceived.getContentObject();
                        } catch (UnreadableException e) {
                            e.printStackTrace();
                        }
                        // definir ce que l'on veut faire du chemin bloqué
                        info.setBlockedPath(sgreceived);
                        info.setBlockStep(4);
                    }
                }
                break;
            case 4:
                List<Couple<String,List<Couple<Observation,Integer>>>> lobs=((AbstractDedaleAgent)this.myAgent).observe();
                String nextPos = info.getNextBlockedPath();
                for (Couple<String, List<Couple<Observation, Integer>>> lob : lobs) {
                    String nodeId = lob.getLeft();
                    if (!nodeId.equals(nextPos)) {
                        nextPos = nodeId;
                        info.setBlockStep(5);
                        break;
                    }
                }
                ((AbstractDedaleAgent)this.myAgent).moveTo(nextPos);
                break;
            case 5:
                try {
                    this.myAgent.doWait(5000);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                info.setBlockStep(-1);
                if(info.isExploEnded()){
                    state = 2;
                }else {
                    state = 1;
                }
                break;
        }
    }

    @Override
    public int onEnd() {
        return state; //3to receive 2 to collect 1 to explo
    }
}
