package eu.su.mas.dedaleEtu.mas.behaviours;

import dataStructures.tuple.Couple;
import eu.su.mas.dedale.env.Observation;
import eu.su.mas.dedale.mas.AbstractDedaleAgent;
import eu.su.mas.dedaleEtu.mas.knowledge.AgentMeta;
import eu.su.mas.dedaleEtu.mas.knowledge.MapRepresentation;
import eu.su.mas.dedaleEtu.mas.knowledge.Position;
import jade.core.behaviours.OneShotBehaviour;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**Comportement d'exploration*/
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
        /*Si explo timed alors on part en collecte*/
        if(this.info.hasExplorationTimedOut()) {
            System.out.println("EXPLORATION TIMED OUT");
            info.setExploEnded();
            info.setTargetNode(null,null);
            state = 2; //-1 = finished
            info.setCollectStep(1);
            System.out.println(myAgent.getLocalName() + " Exploring done");
            return;
        }

        //0) Retrieve the current position
        String myPosition=((AbstractDedaleAgent)this.myAgent).getCurrentPosition();
        this.info.setMyPosition(myPosition);
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
                if (!lob.getRight().isEmpty()){
                    if (lob.getRight().get(0).getLeft() == Observation.STENCH){
                        this.info.addBlockedNode(nodeId);
                    }
                }

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
                System.out.println(myAgent.getLocalName() + " Exploring done");

            } else {
                if (this.info.noTargetNode()) {
                    // try to go for another open node
                    this.info.findTrajectory(myPosition);
                }

                String nextPos = this.info.getNextNode();
                //System.out.println(this.myAgent.getLocalName() +": GO TO " + this.info.getTargetNode() + " FROM " + myPosition + " NEXT NODE " + nextPos);
                if(!Objects.equals(nextPos, "")){
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
                                System.out.println("Blocked " + myAgent.getLocalName() + " AT " + myPosition);
                                state = 6; //Blocked
                            }
                        }
                    }catch(RuntimeException e){
                        System.out.println("DIED at "+ myPosition );
                        this.blockedCounter = this.blockedCounter + 1;
                        if (blockedCounter == 20) {
                            info.setBlockStep(1);
                            System.out.println("Blocked " + myAgent.getLocalName() + " AT " + myPosition);
                            state = 6; //Blocked
                        }
                    }
                }else {
                    this.blockedCounter++;
                    if (blockedCounter == 20) {
                        info.setBlockStep(1);
                        System.out.println("Blocked " + myAgent.getLocalName() + " AT " + myPosition);
                        state = 6; //Blocked
                    }
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
