package edu.sma.agents;

import java.util.concurrent.ThreadLocalRandom;

import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.CyclicBehaviour;
import jade.core.behaviours.TickerBehaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;

public class FloristaAgent extends Agent{
    
    private static final int TIEMPO_TRABAJO_MIN = 10000; // 10 segundos
    private static final int TIEMPO_TRABAJO_MAX = 15000; // 15 segundos
    private static final int MAX_ORDENES = 3;
    private Integer siguienteOrdenPorAtender = 1;

    protected void setup(){

        // Atendemos las consultas REQUEST del recepcionista
        addBehaviour(new CyclicBehaviour(this) {
            @Override
            public void action(){
                ACLMessage msg = receive(MessageTemplate.MatchPerformative(ACLMessage.REQUEST));
                if (msg == null) {
                    block();
                    return;
                }

                String content = msg.getContent();
                System.out.println(getLocalName() + " >> Recibido REQUEST: '" + content + "'");

                int orden = obtenerOrden(content);
                if (orden<0){
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
            
        });

        // Con ticket simulo tiempo de trabajo por arreglo
        long tiempoInicial = ThreadLocalRandom.current().nextLong(TIEMPO_TRABAJO_MIN, TIEMPO_TRABAJO_MAX + 1);
        addBehaviour(new TickerBehaviour(this, tiempoInicial) {
            @Override
            protected void onTick() {
                if (siguienteOrdenPorAtender <= MAX_ORDENES) {
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
                int ordenActual = siguienteOrdenPorAtender;
                System.out.println(myAgent.getLocalName() + " >> Comenzando trabajo en orden " + ordenActual + 
                        " (tiempo: " + getTickCount() + "ms)");

                // Para mantener el sistema reactivo, el trabajo se simula en el tick
                System.out.println(myAgent.getLocalName() + " >> Orden " + ordenActual + " completada");

                // Notificar al recepcionista
                notificarRecepcionista(ordenActual);

                // Incrementar el contador
                siguienteOrdenPorAtender++;
            }

            private void notificarRecepcionista(int orden) {
                ACLMessage inform = new ACLMessage(ACLMessage.INFORM);
                inform.addReceiver(new AID("recepcionistaAgent", AID.ISLOCALNAME));
                inform.setContent("Pedido listo para orden " + orden);
                System.out.println(myAgent.getLocalName() + " >> Enviando INFORM al recepcionista: " +
                        "'Pedido listo para orden " + orden + "'");
                myAgent.send(inform);
            }
        });
    }

    public int obtenerOrden(String mensaje){
        String[] partes = mensaje.split("-");
        int numero_orden = -1;
        try{
            numero_orden = Integer.parseInt(partes[1]);
        }catch (NumberFormatException e) {
            System.out.println("No es un #orden valido");
        }
        
        return numero_orden;
    }

}
