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
import java.util.Random;
/**Comportement de blocage */
public class BlockedBehaviour extends OneShotBehaviour {
    private AgentMeta info;
    private int state;

    public BlockedBehaviour(AbstractDedaleAgent a, AgentMeta info) {
        super(a);
        this.info = info;
    }

    @Override
    public void action() {
        String myPosition = ((AbstractDedaleAgent)myAgent).getCurrentPosition();
        switch (info.getBlockStep()){
            case 1:
                //calcul d'un autre chemin(pas implementé)
            case 2:
                //envoie d'un message de blocage
                BlockedMessage msg = new BlockedMessage(this.myAgent.getAID(),myPosition, Instant.now().toEpochMilli());
                for (String agentName : info.getListReceiverAgents()) {
                    msg.addReceiver(new AID(agentName,AID.ISLOCALNAME));
                }
                ((AbstractDedaleAgent)this.myAgent).sendMessage(msg);
                state = 3; //go to dispatcher
                break;
            case 3:
                //Compare la priorité pour savoir qui va laisser passer
                String receiver = this.info.getLastReceiver();
                if (receiver == null || receiver.equals("asm")){
                    if(info.isExploEnded()){
                        state = 2;
                    }else {
                        state = 1;
                    }
                }
                Hashtable<String, AgentSpecs> specs = info.getAgentSpecsHashtable();
                int myPrio = info.getMySpecs().getPrio();
                if(specs.get(receiver) == null){
                    System.out.println("PRIO ERROR Random move");
                    info.setBlockStep(-1);

                    if (myPosition!=null) {
                        //random walk
                        List<Couple<String, List<Couple<Observation, Integer>>>> lobs = ((AbstractDedaleAgent) this.myAgent).observe();//myPosition

                        try {
                            this.myAgent.doWait(300);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }

                        Random r = new Random();
                        int moveId = 1 + r.nextInt(lobs.size() - 1);
                        ((AbstractDedaleAgent) this.myAgent).moveTo(lobs.get(moveId).getLeft());
                    }


                    if(info.isExploEnded()){
                        state = 2;
                    }else {
                        state = 1;
                    }
                    break;
                }
                int yourPrio = specs.get(receiver).getPrio();
                if(myPrio>yourPrio){
                    /*Si j'ai la priorité j'envoie mon chemin*/
                    List<String> currentTrajectory =  info.getCurrentTrajectory();
                    MyPathMessage msg2 = new MyPathMessage(myAgent.getAID(),currentTrajectory, Instant.now().toEpochMilli());
                    for (String agentName : info.getListReceiverAgents()) {
                        msg2.addReceiver(new AID(agentName,AID.ISLOCALNAME));
                    }
                    ((AbstractDedaleAgent)this.myAgent).sendMessage(msg2);
                }else{
                    /*Si j'ai pas la priorité j'ecoute et j'enregistre ton chemin */
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
                info.setBlockStep(5);
                break;
            case 4:
                /*Je suis le chemin jusqu'a trouver un noeud pas dans le chemin*/
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
                try {
                    this.myAgent.doWait(300);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                break;
            case 5:
                /*Et j'attends*/
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
