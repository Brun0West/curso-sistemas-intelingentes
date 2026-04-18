package edu.sma.agents;

import edu.sma.behaviours.SupervisorMonitorBehaviour;
import edu.sma.common.DfUtils;
import edu.sma.common.ServiceNames;
import jade.core.Agent;
import jade.core.behaviours.TickerBehaviour;

import java.util.ArrayList;
import java.util.List;

public class SupervisorAgent extends Agent {

    private final List<String> auditLog = new ArrayList<>();

    @Override
    protected void setup() {
        DfUtils.registerService(this, ServiceNames.SUPERVISOR_SERVICE, getLocalName() + "-supervision-service");
        System.out.println(getLocalName() + " iniciado y registrado en DF.");

        addBehaviour(new SupervisorMonitorBehaviour(auditLog));

        addBehaviour(new TickerBehaviour(this, 30000) {
            @Override
            protected void onTick() {
                int restaurants = DfUtils.countServices(myAgent, ServiceNames.RESTAURANT_SERVICE);
                int logistics = DfUtils.countServices(myAgent, ServiceNames.LOGISTICS_SERVICE);
                System.out.println("\n===== REPORTE SUPERVISOR =====");
                System.out.println("Eventos auditados: " + auditLog.size());
                System.out.println("restaurant-service en DF: " + restaurants);
                System.out.println("logistics-service en DF: " + logistics);
                if (!auditLog.isEmpty()) {
                    System.out.println("Último evento: " + auditLog.get(auditLog.size() - 1));
                }
                System.out.println("=============================\n");
            }
        });
    }
}
