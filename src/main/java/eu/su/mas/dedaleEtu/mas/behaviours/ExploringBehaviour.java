package eu.su.mas.dedaleEtu.mas.behaviours;

import dataStructures.tuple.Couple;
import eu.su.mas.dedale.env.Observation;
import eu.su.mas.dedale.mas.AbstractDedaleAgent;
import eu.su.mas.dedaleEtu.mas.knowledge.AgentMeta;
import eu.su.mas.dedaleEtu.mas.knowledge.MapRepresentation;
import eu.su.mas.dedaleEtu.mas.knowledge.Position;
import jade.core.behaviours.OneShotBehaviour;
import net.sourceforge.plantuml.Run;
import org.omg.SendingContext.RunTime;

import java.util.ArrayList;
import java.util.List;

public class ExploringBehaviour extends OneShotBehaviour {
    private AgentMeta info;
    private int state;
    private int blockedCounter = 0;

    public ExploringBehaviour(AbstractDedaleAgent a, AgentMeta info) {
        super(a);
        this.info = info;
    }

    @Override
    public void action() {
        state = 0;
        if(this.info.getMyMap() ==null )
            this.info.setMyMap(new MapRepresentation());

        //0) Retrieve the current position
        String myPosition=((AbstractDedaleAgent)this.myAgent).getCurrentPosition();
        this.info.setMyPosition(myPosition);
        // TODO: Smarter target node decision
        if (myPosition!=null){
            //List of observable from the agent's current position
            List<Couple<String,List<Couple<Observation,Integer>>>> lobs=((AbstractDedaleAgent)this.myAgent).observe();//myPosition
            Position.GeneratePositionFromObservations(lobs,info);
            try {
                this.myAgent.doWait(300);
            } catch (Exception e) {
                e.printStackTrace();
            }

            //1) remove the current node from openlist and add it to closedNodes.
            this.info.updatePosition(myPosition);

            this.info.getMyMap().addNode(myPosition, MapRepresentation.MapAttribute.closed);

            //2) get the surrounding nodes and, if not in closedNodes, add them to open nodes.
            for (Couple<String, List<Couple<Observation, Integer>>> lob : lobs) {
                String nodeId = lob.getLeft();
                if (!this.info.getClosedNodes().contains(nodeId)) {
                    this.info.updateMaps(myPosition,nodeId);
                    if (this.info.getTargetNode() == null && !this.info.isNodeBlocked(nodeId))
                        this.info.setTargetNode(nodeId, new ArrayList<String>(){{ add(nodeId); }});
                }
            }

            //3) while openNodes is not empty, continues.
            if (this.info.getOpenNodes().isEmpty()){
                //Explo finished
                info.setExploEnded();
                info.setTargetNode(null,null);
                state = 2; //-1 = finished
                info.setCollectStep(1);

            } else {
                if (!this.info.hasTargetNode()) {
                    // try to go for another open node
                    this.info.findTrajectory(myPosition);
                }

                String nextPos = this.info.getNextNode();
                //System.out.println(this.myAgent.getLocalName() +": GO TO " + this.info.getTargetNode() + " FROM " + myPosition + " NEXT NODE " + nextPos);
                if(nextPos != null){
                    try {
                        if (((AbstractDedaleAgent) this.myAgent).moveTo(nextPos)) {
                            //System.out.println("MOVE SUCCESSFUL TO " + nextPos + " CONFIRM " + myPosition);
                            this.blockedCounter = 0;
                            if (nextPos.equals(this.info.getTargetNode())) {
                                //System.out.println("REACHED NODE " + this.info.getTargetNode());
                                this.info.setTargetReached();
                                //System.out.println("CLEARED TARGET NODE " + this.info.getTargetNode());
                            }
                        } else {
                            //System.out.println("CANCELING MOVE TO " + nextPos);
                            this.info.cancelMove(nextPos);
                            this.blockedCounter = this.blockedCounter + 1;
                            if (blockedCounter == 20) {
                                info.setBlockStep(1);
                                state = 6; //Blocked
                            }
                        }
                    } catch (RuntimeException e) {
                        System.out.println(this.myAgent.getLocalName() + ": DIED WHILE TRYING TO ACCESS: " + nextPos);
                        this.blockedCounter = this.blockedCounter + 1;
                        if (blockedCounter == 20) {
                            info.setBlockStep(1);
                            state = 3; //Blocked
                        }
                    }
                }else {
                    this.blockedCounter++;
                }
            }
        }
        if(info.doPing()){
            state = 1;
        }
        info.switchPing();
    }

    @Override
    public int onEnd() {
        return state; // pinging;
    }
}
