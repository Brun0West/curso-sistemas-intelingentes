package edu.sma.behaviours;

import edu.sma.common.PayloadCodec;
import jade.core.Agent;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.proto.ContractNetResponder;

import java.time.LocalTime;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;

public class RestaurantContractNetResponder extends ContractNetResponder {

    private final String restaurantName;

    public RestaurantContractNetResponder(Agent agent, MessageTemplate template, String restaurantName) {
        super(agent, template);
        this.restaurantName = restaurantName;
    }

    @Override
    protected ACLMessage handleCfp(ACLMessage cfp) {
        Map<String, String> request = PayloadCodec.decode(cfp.getContent());
        String orderId = request.getOrDefault("orderId", "N/A");
        int items = Integer.parseInt(request.getOrDefault("items", "1"));

        double basePrice = 12.0 + (items * 5.5);
        double dynamicFactor = ThreadLocalRandom.current().nextDouble(0.90, 1.15);
        int prepMins = ThreadLocalRandom.current().nextInt(12, 26);
        double total = Math.round(basePrice * dynamicFactor * 100.0) / 100.0;

        Map<String, String> proposalData = new LinkedHashMap<>();
        proposalData.put("orderId", orderId);
        proposalData.put("restaurant", restaurantName);
        proposalData.put("total", String.valueOf(total));
        proposalData.put("prepMins", String.valueOf(prepMins));
        proposalData.put("timestamp", LocalTime.now().toString());

        ACLMessage propose = cfp.createReply();
        propose.setPerformative(ACLMessage.PROPOSE);
        propose.setContent(PayloadCodec.encode(proposalData));
        return propose;
    }

    @Override
    protected ACLMessage handleAcceptProposal(ACLMessage cfp, ACLMessage propose, ACLMessage accept) {
        ACLMessage inform = accept.createReply();
        inform.setPerformative(ACLMessage.INFORM);
        inform.setContent("status=ORDER_ACCEPTED;source=" + restaurantName);
        return inform;
    }

    @Override
    protected void handleRejectProposal(ACLMessage cfp, ACLMessage propose, ACLMessage reject) {
        System.out.println(myAgent.getLocalName() + " >> propuesta rechazada por " + reject.getSender().getLocalName());
    }
}
