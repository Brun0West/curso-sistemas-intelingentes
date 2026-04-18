package edu.sma.behaviours;

import jade.core.behaviours.CyclicBehaviour;
import jade.lang.acl.ACLMessage;

import java.time.LocalDateTime;
import java.util.List;

public class SupervisorMonitorBehaviour extends CyclicBehaviour {

    private final List<String> auditLog;

    public SupervisorMonitorBehaviour(List<String> auditLog) {
        this.auditLog = auditLog;
    }

    @Override
    public void action() {
        ACLMessage msg = myAgent.receive();
        if (msg == null) {
            block();
            return;
        }

        String entry = "%s | from=%s | perf=%s | conv=%s | content=%s".formatted(
                LocalDateTime.now(),
                msg.getSender().getName(),
                ACLMessage.getPerformative(msg.getPerformative()),
                msg.getConversationId(),
                msg.getContent()
        );
        auditLog.add(entry);
        System.out.println(myAgent.getLocalName() + " AUDIT >> " + entry);
    }
}
