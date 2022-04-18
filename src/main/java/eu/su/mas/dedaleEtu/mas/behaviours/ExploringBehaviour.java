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

public class ExploringBehaviour extends OneShotBehaviour {
    private AgentMeta info;
    private int state;

    public ExploringBehaviour(AbstractDedaleAgent a, AgentMeta info) {
        super(a);
        this.info = info;
    }

    @Override
    public void action() {
        state = 1;
        if(this.info.getMyMap() ==null )
            this.info.setMyMap(new MapRepresentation());

        //0) Retrieve the current position
        String myPosition=((AbstractDedaleAgent)this.myAgent).getCurrentPosition();

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
                state = -1; //-1 = finished
                System.out.println("Exploration successufully done, behaviour removed.");
            } else {
                if(!this.info.hasTargetNode()){
                    // try to go for another open node
                    for(String n : this.info.getOpenNodes()){
                        List<String> pathToNode = this.info.getMyMap().getShortestPath(myPosition, n);
                        boolean discard = false;
                        //TODO BUG when reaching a case ocupped by a wumpus
                        for(String step : pathToNode){
                            if(this.info.isNodeBlocked(step)){
                                discard = true;
                                break;
                            };
                        }
                        if(discard) continue;
                        this.info.setTargetNode(n, pathToNode);
                        break;
                    }
                }


                /*
                //4) select next move.
                //4.1 If there exist one open node directly reachable, go for it,
                //	 otherwise choose one from the openNode list, compute the shortestPath and go for it
                if (nextNode==null) {
                    //no directly accessible openNode
                    //chose one, compute the path and take the first step.
                    nextNode = this.info.getMyMap().getShortestPath(myPosition, this.info.getOpenNodes().get(0)).get(0);
                }*/
            }

            ((AbstractDedaleAgent)this.myAgent).moveTo(this.info.getNextNode());
        }
    }

    @Override
    public int onEnd() {
        return state; // pinging;
    }
}
