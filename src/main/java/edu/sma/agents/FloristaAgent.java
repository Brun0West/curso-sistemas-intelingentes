package edu.sma.agents;

import edu.sma.common.DfUtils;
import edu.sma.common.ServiceNames;
import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.CyclicBehaviour;
import jade.core.behaviours.TickerBehaviour;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;

import java.util.Optional;
import java.util.concurrent.ThreadLocalRandom;

public class FloristaAgent extends Agent {

    private static final int TIEMPO_TRABAJO_MIN = 10000; // 10 segundos
    private static final int TIEMPO_TRABAJO_MAX = 15000; // 15 segundos
    private static final int MAX_ORDENES = 3;

    private int siguienteOrdenPorAtender = 1;
    private AID recepcionistaAid = null;

    @Override
    protected void setup() {
        System.out.println(getLocalName() + " iniciado");

        // Registrar en DF
        DfUtils.registerService(this, ServiceNames.FLORISTA_SERVICE, getLocalName() + "-florista-service");

        // Buscar al recepcionista en el DF
        Optional<DFAgentDescription> recepcionistaOpt = DfUtils.searchFirst(this, ServiceNames.RECEPCIONISTA_SERVICE);
        if (recepcionistaOpt.isPresent()) {
            recepcionistaAid = recepcionistaOpt.get().getName();
            System.out.println(getLocalName() + " >> Recepcionista encontrado: " + recepcionistaAid.getName());
        } else {
            System.err.println(getLocalName() + " >> ERROR: Recepcionista no encontrado en DF");
        }

        // CyclicBehaviour para atender REQUEST del recepcionista
        addBehaviour(new CyclicBehaviour(this) {
            @Override
            public void action() {
                ACLMessage msg = receive(MessageTemplate.MatchPerformative(ACLMessage.REQUEST));
                if (msg == null) {
                    block();
                    return;
                }

                String content = msg.getContent();
                System.out.println(getLocalName() + " >> Recibido REQUEST: '" + content + "'");

                // Extraer orden del contenido (ej. "estaListo? orden=1")
                int orden = extraerOrden(content);
                if (orden < 0) {
                    System.err.println(getLocalName() + " >> ERROR: No se pudo extraer la orden del contenido");
                    return;
                }

                responderAlRecepcionista(msg, orden);
            }

            private void responderAlRecepcionista(ACLMessage msg, int orden) {
                ACLMessage reply = msg.createReply();

                if (orden < siguienteOrdenPorAtender) {
                    // Ya se terminó ese pedido
                    reply.setPerformative(ACLMessage.INFORM);
                    reply.setContent("SI");
                    System.out.println(getLocalName() + " >> INFORM (ya terminado): orden " + orden);
                } else {
                    // Aún no se alcanza ese turno
                    reply.setPerformative(ACLMessage.REFUSE);
                    reply.setContent("NO");
                    System.out.println(getLocalName() + " >> REFUSE (aún no): orden " + orden + 
                            " (siguiente a atender: " + siguienteOrdenPorAtender + ")");
                }

                myAgent.send(reply);
            }

            private int extraerOrden(String content) {
                try {
                    int index = content.indexOf("orden=");
                    if (index >= 0) {
                        String numStr = content.substring(index + 6).split("[ ,;]")[0];
                        return Integer.parseInt(numStr);
                    }
                } catch (NumberFormatException e) {
                    System.err.println(getLocalName() + " >> ERROR al parsear orden: " + e.getMessage());
                }
                return -1;
            }
        });

        // TickerBehaviour para la producción autónoma
        long tiempoInicial = ThreadLocalRandom.current().nextLong(TIEMPO_TRABAJO_MIN, TIEMPO_TRABAJO_MAX + 1);
        addBehaviour(new TickerBehaviour(this, tiempoInicial) {
            @Override
            protected void onTick() {
                if (((FloristaAgent) myAgent).siguienteOrdenPorAtender <= MAX_ORDENES) {
                    producirArreglo();
                    // Ajustar el siguiente intervalo aleatorio
                    long siguienteTiempo = ThreadLocalRandom.current().nextLong(TIEMPO_TRABAJO_MIN, TIEMPO_TRABAJO_MAX + 1);
                    reset(siguienteTiempo);
                } else {
                    System.out.println(myAgent.getLocalName() + " >> Todas las órdenes completadas (máximo " + MAX_ORDENES + ")");
                    stop();
                }
            }

            private void producirArreglo() {
                int ordenActual = ((FloristaAgent) myAgent).siguienteOrdenPorAtender;
                System.out.println(myAgent.getLocalName() + " >> Comenzando trabajo en orden " + ordenActual + 
                        " (tiempo: " + getTickCount() + "ms)");

                // Simular el trabajo (en un escenario real, sería asincrónico)
                // Para mantener el sistema reactivo, el trabajo se simula en el tick
                System.out.println(myAgent.getLocalName() + " >> Orden " + ordenActual + " completada");

                // Notificar al recepcionista
                notificarRecepcionista(ordenActual);

                // Incrementar el contador
                ((FloristaAgent) myAgent).siguienteOrdenPorAtender++;
            }

            private void notificarRecepcionista(int orden) {
                if (((FloristaAgent) myAgent).recepcionistaAid != null) {
                    ACLMessage inform = new ACLMessage(ACLMessage.INFORM);
                    inform.addReceiver(((FloristaAgent) myAgent).recepcionistaAid);
                    inform.setContent("Pedido listo para orden " + orden);
                    inform.setOntology("FLORERIA");
                    System.out.println(myAgent.getLocalName() + " >> Enviando INFORM al recepcionista: " +
                            "'Pedido listo para orden " + orden + "'");
                    myAgent.send(inform);
                } else {
                    System.err.println(myAgent.getLocalName() + " >> ERROR: No hay recepcionista para notificar");
                }
            }
        });
    }

    @Override
    protected void takeDown() {
        System.out.println(getLocalName() + " >> Agente finalizado. Órdenes completadas: " + (siguienteOrdenPorAtender - 1));
    }
}
