package edu.upc.fib.sid.behaviours.factories;

import edu.upc.fib.sid.helpers.Constants;
import edu.upc.fib.sid.helpers.DFUtils;
import edu.upc.fib.sid.helpers.Globals;
import edu.upc.fib.sid.models.WaterTank;
import edu.upc.fib.sid.ontology.BesosRiverOntology;
import edu.upc.fib.sid.ontology.ifaces.RequestPour;
import edu.upc.fib.sid.ontology.impl.RequestPourImpl;
import jade.content.AgentAction;
import jade.content.Predicate;
import jade.content.abs.AbsPredicate;
import jade.content.lang.sl.SL0Vocabulary;
import jade.content.lang.sl.SLVocabulary;
import jade.content.onto.basic.Action;
import jade.core.behaviours.OneShotBehaviour;
import jade.domain.FIPANames;
import jade.lang.acl.ACLMessage;
import jade.util.Logger;

import static edu.upc.fib.sid.helpers.LoggerUtils.log;
import static edu.upc.fib.sid.helpers.ReflectionUtils.invokeMethod;

public class FactoryPourWaterBehaviour extends OneShotBehaviour {
    private Logger logger = Logger.getMyLogger(this.getClass().getName());
    private Boolean messageSent = false;

    @Override
    public void action() {
        if (!messageSent) {
            ACLMessage msg = new ACLMessage(ACLMessage.QUERY_IF);
            msg.addReceiver(Globals.TreatmentPlantAID);
            msg.setLanguage(FIPANames.ContentLanguage.FIPA_SL0);
            msg.setOntology(BesosRiverOntology.getInstance().getName());
            msg.setContent("Can I pour water?");
            myAgent.send(msg);
            messageSent = true;
            log(logger, Logger.INFO, "Factory queries to pour water through the sewage system");
        }

        ACLMessage msg = myAgent.receive();
        while (msg == null) {
            block();
            msg = myAgent.receive();
        }

        String senderType = DFUtils.getTypeByAID(myAgent, msg.getSender());
        if (Constants.TREATMENT_PLANT.equals(senderType)) {
            if (msg.getPerformative() == ACLMessage.CONFIRM) {
                ACLMessage reply = msg.createReply();
                reply.setPerformative(ACLMessage.INFORM);
                reply.setContent("I pour waste water");
                myAgent.send(reply);

                WaterTank waterTank = (WaterTank) invokeMethod(myAgent, "getWasteWaterTank");
                waterTank.empty();

                log(logger, Logger.INFO, "Factory pours water through the sewage system");
            } else if (msg.getPerformative() == ACLMessage.DISCONFIRM)
                log(logger, Logger.INFO, "Factory can't pour water through the sewage system");
            invokeMethod(myAgent, "setWaitingWaterPouring", Boolean.FALSE);
        }
    }
}
