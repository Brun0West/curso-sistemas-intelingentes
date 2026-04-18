package edu.sma.agents;

import edu.sma.behaviours.OrderCycleBehaviour;
import edu.sma.common.DfUtils;
import edu.sma.common.ServiceNames;
import jade.core.Agent;
import jade.core.behaviours.TickerBehaviour;

public class ClientAgent extends Agent {

    @Override
    protected void setup() {
        DfUtils.registerService(this, ServiceNames.CLIENT_SERVICE, getLocalName() + "-ordering-service");
        System.out.println(getLocalName() + " iniciado y registrado en DF.");

        Object[] args = getArguments();
        String remoteMtp = args != null && args.length > 0 ? String.valueOf(args[0]) : "http://p1-main:7778/acc";
        String restaurantGuid = args != null && args.length > 1 ? String.valueOf(args[1]) : "restaurante@P1";
        String logisticsGuid = args != null && args.length > 2 ? String.valueOf(args[2]) : "logistica@P1";
        String supervisorGuid = args != null && args.length > 3 ? String.valueOf(args[3]) : "supervisor@P1";

        addBehaviour(new TickerBehaviour(this, 20000) {
            @Override
            protected void onTick() {
                int payments = DfUtils.countServices(myAgent, ServiceNames.PAYMENT_SERVICE);
                System.out.println(getLocalName() + " DF check >> payment-service encontrados: " + payments);
                myAgent.addBehaviour(new OrderCycleBehaviour(myAgent, remoteMtp, restaurantGuid, logisticsGuid, supervisorGuid));
            }
        });
    }
}
