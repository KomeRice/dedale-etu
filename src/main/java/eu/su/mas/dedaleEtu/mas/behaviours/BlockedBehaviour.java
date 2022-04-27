package eu.su.mas.dedaleEtu.mas.behaviours;

import eu.su.mas.dedale.mas.AbstractDedaleAgent;
import eu.su.mas.dedaleEtu.mas.knowledge.AgentMeta;
import jade.core.behaviours.OneShotBehaviour;

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

    }

    @Override
    public int onEnd() {
        return state; //3to receive 1 to explo
    }
}
