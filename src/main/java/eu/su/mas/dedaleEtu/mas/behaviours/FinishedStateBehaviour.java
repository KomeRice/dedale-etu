package eu.su.mas.dedaleEtu.mas.behaviours;

import jade.core.behaviours.OneShotBehaviour;

public class FinishedStateBehaviour extends OneShotBehaviour {

    private int done = -1;

    @Override
    public void action() {

    }

    @Override
    public int onEnd() {
        return done;
    }
}
