package eu.su.mas.dedaleEtu.mas.behaviours;

import dataStructures.tuple.Couple;
import eu.su.mas.dedale.env.Observation;
import eu.su.mas.dedale.mas.AbstractDedaleAgent;
import eu.su.mas.dedaleEtu.mas.knowledge.AgentMeta;
import jade.core.Agent;
import jade.core.behaviours.OneShotBehaviour;

import java.io.IOException;
import java.util.List;
import java.util.Random;

public class FinishedStateBehaviour extends OneShotBehaviour {

    private int done = -1;
    private int state;
    private AgentMeta info;

    public FinishedStateBehaviour(final Agent a, AgentMeta info) {
        super(a);
        this.info = info;
    }

    @Override
    public void action() {
        String myPosition=((AbstractDedaleAgent)this.myAgent).getCurrentPosition();
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
    }

    @Override
    public int onEnd() {
        return 1 ; //finished
    }
}
