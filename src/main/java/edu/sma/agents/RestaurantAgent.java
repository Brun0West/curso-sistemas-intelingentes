package edu.sma.agents;

import edu.sma.behaviours.RestaurantContractNetResponder;
import edu.sma.common.DfUtils;
import edu.sma.common.PayloadCodec;
import edu.sma.common.ServiceNames;
import jade.core.Agent;
import jade.core.behaviours.CyclicBehaviour;
import jade.core.behaviours.TickerBehaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;

import java.util.Map;

public class RestaurantAgent extends Agent {

    @Override
    protected void setup() {
        DfUtils.registerService(this, ServiceNames.RESTAURANT_SERVICE, getLocalName() + "-menu-service");
        System.out.println(getLocalName() + " iniciado y registrado en DF.");

        MessageTemplate cfpTemplate = MessageTemplate.MatchPerformative(ACLMessage.CFP);
        addBehaviour(new RestaurantContractNetResponder(this, cfpTemplate, getLocalName()));

        addBehaviour(new CyclicBehaviour() {
            @Override
            public void action() {
                ACLMessage msg = receive(MessageTemplate.MatchPerformative(ACLMessage.INFORM));
                if (msg == null) {
                    block();
                    return;
                }
                Map<String, String> payload = PayloadCodec.decode(msg.getContent());
                if ("CONFIRMED".equalsIgnoreCase(payload.getOrDefault("status", ""))) {
                    System.out.println(getLocalName() + " >> pedido confirmado: " + payload.get("orderId"));
                }
            }
        });

        addBehaviour(new TickerBehaviour(this, 15000) {
            @Override
            protected void onTick() {
                int logisticsCount = DfUtils.countServices(myAgent, ServiceNames.LOGISTICS_SERVICE);
                System.out.println(getLocalName() + " DF check >> logistics-service encontrados: " + logisticsCount);
            }
        });
    }
}
