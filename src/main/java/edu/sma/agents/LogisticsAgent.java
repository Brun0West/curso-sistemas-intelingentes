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

public class LogisticsAgent extends Agent {

    @Override
    protected void setup() {
        DfUtils.registerService(this, ServiceNames.LOGISTICS_SERVICE, getLocalName() + "-delivery-service");
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
                int prepMins = Integer.parseInt(req.getOrDefault("prepMins", "15"));
                int trafficFactor = ThreadLocalRandom.current().nextInt(5, 18);
                int eta = prepMins + trafficFactor;
                double deliveryCost = Math.round((3.5 + (trafficFactor * 0.18)) * 100.0) / 100.0;

                Map<String, String> payload = new LinkedHashMap<>();
                payload.put("orderId", req.getOrDefault("orderId", "N/A"));
                payload.put("etaMins", String.valueOf(eta));
                payload.put("deliveryCost", String.valueOf(deliveryCost));
                payload.put("route", "Ruta-Optimizada-Zona-Centro");

                ACLMessage reply = msg.createReply();
                reply.setPerformative(ACLMessage.INFORM);
                reply.setContent(PayloadCodec.encode(payload));
                send(reply);

                System.out.println(getLocalName() + " >> cálculo entrega enviado: " + payload);
            }
        });

        addBehaviour(new TickerBehaviour(this, 15000) {
            @Override
            protected void onTick() {
                int restaurants = DfUtils.countServices(myAgent, ServiceNames.RESTAURANT_SERVICE);
                System.out.println(getLocalName() + " DF check >> restaurant-service encontrados: " + restaurants);
            }
        });
    }
}
