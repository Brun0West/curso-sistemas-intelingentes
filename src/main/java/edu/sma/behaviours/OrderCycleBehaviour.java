package edu.sma.behaviours;

import edu.sma.common.DfUtils;
import edu.sma.common.PayloadCodec;
import edu.sma.common.RemoteAidFactory;
import edu.sma.common.ServiceNames;
import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.OneShotBehaviour;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.proto.ContractNetInitiator;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Vector;
import java.util.concurrent.ThreadLocalRandom;

public class OrderCycleBehaviour extends OneShotBehaviour {

    private final String remoteMtp;
    private final String restaurantGuid;
    private final String logisticsGuid;
    private final String supervisorGuid;

    public OrderCycleBehaviour(Agent a, String remoteMtp, String restaurantGuid, String logisticsGuid, String supervisorGuid) {
        super(a);
        this.remoteMtp = remoteMtp;
        this.restaurantGuid = restaurantGuid;
        this.logisticsGuid = logisticsGuid;
        this.supervisorGuid = supervisorGuid;
    }

    @Override
    public void action() {
        String orderId = "ORD-" + System.currentTimeMillis();

        AID restaurantAid = RemoteAidFactory.build(restaurantGuid, remoteMtp);
        AID logisticsAid = RemoteAidFactory.build(logisticsGuid, remoteMtp);
        AID supervisorAid = RemoteAidFactory.build(supervisorGuid, remoteMtp);

        int items = ThreadLocalRandom.current().nextInt(1, 5);

        ACLMessage cfp = new ACLMessage(ACLMessage.CFP);
        cfp.addReceiver(restaurantAid);
        cfp.setConversationId(orderId + "-menu");
        cfp.setOntology("SMA-ORDERS");

        Map<String, String> cfpData = new LinkedHashMap<>();
        cfpData.put("orderId", orderId);
        cfpData.put("items", String.valueOf(items));
        cfpData.put("requestedAt", LocalDateTime.now().toString());
        cfp.setContent(PayloadCodec.encode(cfpData));

        myAgent.addBehaviour(new ContractNetInitiator(myAgent, cfp) {
            @Override
            protected void handlePropose(ACLMessage propose, Vector acceptances) {
                Map<String, String> proposal = PayloadCodec.decode(propose.getContent());
                System.out.println(myAgent.getLocalName() + " >> propuesta recibida: " + proposal);

                ACLMessage accept = propose.createReply();
                accept.setPerformative(ACLMessage.ACCEPT_PROPOSAL);
                acceptances.add(accept);

                requestDeliveryAndPayment(orderId, proposal, logisticsAid, supervisorAid);
            }

            @Override
            protected void handleRefuse(ACLMessage refuse) {
                System.out.println(myAgent.getLocalName() + " >> restaurante rechazó CFP: " + refuse.getContent());
            }

            @Override
            protected void handleFailure(ACLMessage failure) {
                System.out.println(myAgent.getLocalName() + " >> fallo en CFP: " + failure.getContent());
            }
        });
    }

    private void requestDeliveryAndPayment(String orderId,
                                           Map<String, String> proposal,
                                           AID logisticsAid,
                                           AID supervisorAid) {
        ACLMessage deliveryReq = new ACLMessage(ACLMessage.REQUEST);
        deliveryReq.addReceiver(logisticsAid);
        deliveryReq.setConversationId(orderId + "-delivery");
        deliveryReq.setOntology("SMA-ORDERS");

        Map<String, String> deliveryData = new LinkedHashMap<>();
        deliveryData.put("orderId", orderId);
        deliveryData.put("prepMins", proposal.getOrDefault("prepMins", "15"));
        deliveryData.put("zone", "Sector-Centro");
        deliveryReq.setContent(PayloadCodec.encode(deliveryData));
        myAgent.send(deliveryReq);

        ACLMessage deliveryInform = myAgent.blockingReceive(
                MessageTemplate.MatchConversationId(orderId + "-delivery"), 7000
        );

        if (deliveryInform == null || deliveryInform.getPerformative() != ACLMessage.INFORM) {
            notifySupervisor(supervisorAid, orderId, "DELIVERY_UNAVAILABLE");
            return;
        }

        Map<String, String> deliveryInfo = PayloadCodec.decode(deliveryInform.getContent());
        DFAgentDescription paymentDf = DfUtils.searchFirst(myAgent, ServiceNames.PAYMENT_SERVICE).orElse(null);
        if (paymentDf == null) {
            notifySupervisor(supervisorAid, orderId, "PAYMENT_SERVICE_NOT_FOUND");
            return;
        }

        ACLMessage paymentReq = new ACLMessage(ACLMessage.REQUEST);
        paymentReq.addReceiver(paymentDf.getName());
        paymentReq.setConversationId(orderId + "-payment");
        paymentReq.setOntology("SMA-ORDERS");

        Map<String, String> paymentData = new LinkedHashMap<>();
        paymentData.put("orderId", orderId);
        paymentData.put("amount", proposal.getOrDefault("total", "0"));
        paymentData.put("deliveryFee", deliveryInfo.getOrDefault("deliveryCost", "0"));
        paymentData.put("method", "CARD");
        paymentReq.setContent(PayloadCodec.encode(paymentData));
        myAgent.send(paymentReq);

        ACLMessage paymentResp = myAgent.blockingReceive(
                MessageTemplate.MatchConversationId(orderId + "-payment"), 7000
        );

        if (paymentResp == null) {
            notifySupervisor(supervisorAid, orderId, "PAYMENT_TIMEOUT");
            return;
        }

        if (paymentResp.getPerformative() == ACLMessage.CONFIRM) {
            notifyRestaurantConfirmed(orderId, proposal);
            notifySupervisor(supervisorAid, orderId, "ORDER_COMPLETED");
            System.out.println(myAgent.getLocalName() + " >> pedido completado " + orderId);
        } else {
            notifySupervisor(supervisorAid, orderId, "PAYMENT_REJECTED");
            System.out.println(myAgent.getLocalName() + " >> pago rechazado para " + orderId);
        }
    }

    private void notifyRestaurantConfirmed(String orderId, Map<String, String> proposal) {
        ACLMessage msg = new ACLMessage(ACLMessage.INFORM);
        msg.addReceiver(RemoteAidFactory.build(restaurantGuid, remoteMtp));
        msg.setConversationId(orderId + "-confirmed");

        Map<String, String> payload = new LinkedHashMap<>();
        payload.put("orderId", orderId);
        payload.put("status", "CONFIRMED");
        payload.put("restaurant", proposal.getOrDefault("restaurant", "N/A"));
        msg.setContent(PayloadCodec.encode(payload));
        myAgent.send(msg);
    }

    private void notifySupervisor(AID supervisorAid, String orderId, String status) {
        ACLMessage inform = new ACLMessage(ACLMessage.INFORM);
        inform.addReceiver(supervisorAid);
        inform.setConversationId(orderId + "-supervision");

        Map<String, String> payload = new LinkedHashMap<>();
        payload.put("orderId", orderId);
        payload.put("status", status);
        payload.put("agent", myAgent.getLocalName());
        inform.setContent(PayloadCodec.encode(payload));
        myAgent.send(inform);
    }
}
