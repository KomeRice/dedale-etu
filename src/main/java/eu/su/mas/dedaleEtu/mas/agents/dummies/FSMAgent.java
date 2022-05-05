package eu.su.mas.dedaleEtu.mas.agents.dummies;

import eu.su.mas.dedale.mas.AbstractDedaleAgent;
import eu.su.mas.dedale.mas.agent.behaviours.startMyBehaviours;
import eu.su.mas.dedaleEtu.mas.behaviours.*;
import eu.su.mas.dedaleEtu.mas.knowledge.AgentMeta;
import eu.su.mas.dedaleEtu.mas.knowledge.AgentSpecs;
import eu.su.mas.dedaleEtu.mas.knowledge.MapRepresentation;
import jade.core.behaviours.Behaviour;
import jade.core.behaviours.FSMBehaviour;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class FSMAgent extends AbstractDedaleAgent {
    private static final long serialVersionUID = -7969469610271668140L;
    private AgentMeta info;

    protected void setup() {

        super.setup();


        //get the parameters added to the agent at creation (if any)
        final Object[] args = getArguments();

        List<String> list_agentNames=new ArrayList<>();
        AgentSpecs ags = null;
        if(args.length==0){
            System.err.println("Error while creating the agent, names of agent to contact expected");
            System.exit(-1);
        }else{
//            int i=2;// WARNING YOU SHOULD ALWAYS START AT 2. This will be corrected in the next release.
//            while (i<args.length) {
//                list_agentNames.add((String)args[i]);
//                i++;
//            }
            ags = new AgentSpecs((Integer[]) args[2]);
            list_agentNames.addAll(Arrays.asList((String[]) args[3]));

        }

        info = new AgentMeta(list_agentNames);
        info.setSpecs(ags);
        List<Behaviour> lb=new ArrayList<>();


        FSMBehaviour behaviours = new FSMBehaviour(this);

        behaviours.registerFirstState(new ExploringBehaviour(this,info),"Exploring");
        behaviours.registerState(new SendPingBehaviour(this,info),"Pinging");
        behaviours.registerState(new DispatcherBehaviour(this,info),"Receiving");
        behaviours.registerState(new ShareMapBehaviour(this,info),"Sharing");
        behaviours.registerState(new FinishedStateBehaviour(this,info),"Finished");
        behaviours.registerState(new BlockedBehaviour(this,info),"Blocked");
        behaviours.registerState(new CollectingBehavior(this,info),"Collecting");
        behaviours.registerState(new FirstMetBehaviour(this,info),"First");

        behaviours.registerTransition("Exploring","Pinging",1);
        behaviours.registerTransition("Exploring","Collecting",2);
        behaviours.registerTransition("Exploring","Blocked",6);
        behaviours.registerTransition("Exploring","Exploring",0);

        behaviours.registerTransition("Pinging","Receiving",2);
        behaviours.registerTransition("Sharing","Exploring",727);
        behaviours.registerTransition("Sharing","Finished",-1);
        behaviours.registerTransition("Sharing","Collecting",2);

        behaviours.registerTransition("Receiving","Exploring",3);
        behaviours.registerTransition("Receiving","Sharing",727);
        behaviours.registerTransition("Receiving","Blocked",9);
        behaviours.registerTransition("Receiving","First",10);
        behaviours.registerTransition("Receiving","Collecting",4);
        behaviours.registerTransition("Receiving","Finished",-1);

        behaviours.registerTransition("Blocked","Receiving",3);
        behaviours.registerTransition("Blocked","Exploring",1);
        behaviours.registerTransition("Blocked","Collecting",2);
        behaviours.registerTransition("Blocked","Blocked",0);

        behaviours.registerTransition("First","Sharing",1);
        behaviours.registerTransition("First","Blocked",2);

        behaviours.registerTransition("Collecting","Blocked",3);
        behaviours.registerTransition("Collecting","Receiving",2);
        behaviours.registerTransition("Collecting","Collecting",0);
        behaviours.registerTransition("Collecting","Finished",-1);

        behaviours.registerTransition("Finished","Pinging",1);

        //behaviours.registerTransition("Exploring","Exploring",1);



        lb.add(behaviours);
        addBehaviour(new startMyBehaviours(this, lb));

        System.out.println("the  agent " + this.getLocalName() + " is started");
    }
}
