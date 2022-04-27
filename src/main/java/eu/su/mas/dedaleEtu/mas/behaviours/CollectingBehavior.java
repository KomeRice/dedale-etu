package eu.su.mas.dedaleEtu.mas.behaviours;

import eu.su.mas.dedale.mas.AbstractDedaleAgent;
import eu.su.mas.dedaleEtu.mas.knowledge.AgentMeta;
import jade.core.behaviours.OneShotBehaviour;

public class CollectingBehavior extends OneShotBehaviour {
    private AgentMeta info;
    private int state;

    //step 1 return to rdv point
    //step 2 share prio and backpack capacity
    //step 3 the highest prio do the plan :
        //the backpack capacity the closest to the resources
    //step 4 go to the assigned locations
        //if encountering someone not in the plan, tell him which resource are taken, and the rdv point
        //if 2 different teams member encounter the highest prio one take the resources,
            // and the lowest one recalculate another path,
        //if encounter a not repertoiried node register the resource
    //step 5
        //if backpack full done
        //if not full (treasure stolen or not enough info)-> return to the rdv point to wait


    public CollectingBehavior(AbstractDedaleAgent a, AgentMeta info) {
        super(a);
        this.info = info;
    }

    @Override
    public void action() {
        switch (info.getCollectStep()){
            case 0:
                info.setTargetNode(info.getRdvPoint(),info.getMyMap().getShortestPath(info.getMyPosition(), info.getRdvPoint()));
                String nextPos = info.getNextNode();

                break;
        }
    }

    @Override
    public int onEnd() {
        return state;
    }
}
