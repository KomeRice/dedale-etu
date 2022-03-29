package eu.su.mas.dedaleEtu.mas.agents.dummies;

import eu.su.mas.dedale.mas.AbstractDedaleAgent;
import eu.su.mas.dedale.mas.agent.behaviours.startMyBehaviours;
import eu.su.mas.dedaleEtu.mas.behaviours.ExploCoopBehaviour;
import eu.su.mas.dedaleEtu.mas.behaviours.ExploSoloBehaviour;
import eu.su.mas.dedaleEtu.mas.behaviours.ExploringBehaviour;
import eu.su.mas.dedaleEtu.mas.behaviours.SendPingBehaviour;
import eu.su.mas.dedaleEtu.mas.knowledge.AgentMeta;
import eu.su.mas.dedaleEtu.mas.knowledge.MapRepresentation;
import jade.core.behaviours.Behaviour;
import jade.core.behaviours.FSMBehaviour;

import java.util.ArrayList;
import java.util.List;

public class FSMAgent extends AbstractDedaleAgent {
    private static final long serialVersionUID = -7969469610271668140L;
    private AgentMeta info;

    protected void setup() {

        super.setup();


        //get the parameters added to the agent at creation (if any)
        final Object[] args = getArguments();

        List<String> list_agentNames=new ArrayList<String>();

        if(args.length==0){
            System.err.println("Error while creating the agent, names of agent to contact expected");
            System.exit(-1);
        }else{
            int i=2;// WARNING YOU SHOULD ALWAYS START AT 2. This will be corrected in the next release.
            while (i<args.length) {
                list_agentNames.add((String)args[i]);
                i++;
            }
        }

        info = new AgentMeta(list_agentNames);

        List<Behaviour> lb=new ArrayList<Behaviour>();


        FSMBehaviour behaviours = new FSMBehaviour(this);

        behaviours.registerFirstState(new ExploringBehaviour(this,info),"Exploring");
        behaviours.registerState(new SendPingBehaviour(this,info),"Pinging");

        behaviours.registerTransition("Exploring","Pinging",1);
        behaviours.registerTransition("Pinging","Exploring",2);



        lb.add(behaviours);
        addBehaviour(new startMyBehaviours(this, lb));

        System.out.println("the  agent " + this.getLocalName() + " is started");
    }
}
