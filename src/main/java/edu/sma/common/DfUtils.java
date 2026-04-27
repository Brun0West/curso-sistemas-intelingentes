package edu.sma.common;

import jade.core.Agent;
import jade.domain.DFService;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.domain.FIPAException;

import java.util.Arrays;
import java.util.Optional;

public final class DfUtils {

    private DfUtils() {
    }

    public static void registerService(Agent agent, String serviceType, String serviceName) {
        DFAgentDescription dfd = new DFAgentDescription();
        dfd.setName(agent.getAID());

        ServiceDescription sd = new ServiceDescription();
        sd.setType(serviceType);
        sd.setName(serviceName);
        dfd.addServices(sd);

        try {
            DFService.register(agent, dfd);
        } catch (FIPAException e) {
            throw new IllegalStateException("No se pudo registrar el servicio en DF: " + serviceType, e);
        }
    }

    public static Optional<DFAgentDescription> searchFirst(Agent agent, String serviceType) {
        DFAgentDescription template = new DFAgentDescription();
        ServiceDescription sd = new ServiceDescription();
        sd.setType(serviceType);
        template.addServices(sd);

        try {
            DFAgentDescription[] results = DFService.search(agent, template);
            return Arrays.stream(results).findFirst();
        } catch (FIPAException e) {
            return Optional.empty();
        }
    }

    public static int countServices(Agent agent, String serviceType) {
        DFAgentDescription template = new DFAgentDescription();
        ServiceDescription sd = new ServiceDescription();
        sd.setType(serviceType);
        template.addServices(sd);
        try {
            return DFService.search(agent, template).length;
        } catch (FIPAException e) {
            return 0;
        }
    }
}