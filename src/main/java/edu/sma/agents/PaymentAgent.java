package edu.sma.agents;

import edu.sma.common.DfUtils;
import edu.sma.common.PayloadCodec;
import edu.sma.common.ServiceNames;
import jade.core.Agent;
import jade.core.behaviours.CyclicBehaviour;
import jade.core.behaviours.TickerBehaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;

public class PaymentAgent extends Agent {

    @Override
    protected void setup() {
        DfUtils.registerService(this, ServiceNames.PAYMENT_SERVICE, getLocalName() + "-payment-service");
        System.out.println(getLocalName() + " iniciado y registrado en DF.");

        addBehaviour(new CyclicBehaviour() {
            @Override
            public void action() {
                ACLMessage msg = receive(MessageTemplate.MatchPerformative(ACLMessage.REQUEST));
                if (msg == null) {
                    block();
                    return;
                }

                Map<String, String> req = PayloadCodec.decode(msg.getContent());
                double amount = Double.parseDouble(req.getOrDefault("amount", "0"));
                double delivery = Double.parseDouble(req.getOrDefault("deliveryFee", "0"));
                boolean approved = (amount + delivery) < 80.0 || ThreadLocalRandom.current().nextBoolean();

                ACLMessage reply = msg.createReply();
                Map<String, String> payload = new LinkedHashMap<>();
                payload.put("orderId", req.getOrDefault("orderId", "N/A"));
                payload.put("approved", String.valueOf(approved));
                payload.put("totalCharged", String.valueOf(amount + delivery));
                reply.setContent(PayloadCodec.encode(payload));

                if (approved) {
                    reply.setPerformative(ACLMessage.CONFIRM);
                } else {
                    reply.setPerformative(ACLMessage.FAILURE);
                }

                send(reply);
                System.out.println(getLocalName() + " >> validación pago enviada: approved=" + approved);
            }
        });

        addBehaviour(new TickerBehaviour(this, 15000) {
            @Override
            protected void onTick() {
                int clients = DfUtils.countServices(myAgent, ServiceNames.CLIENT_SERVICE);
                System.out.println(getLocalName() + " DF check >> client-service encontrados: " + clients);
            }
        });
    }
}
