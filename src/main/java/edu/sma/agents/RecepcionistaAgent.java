package edu.sma.agents;
import edu.sma.common.ServiceNames;
import jade.core.Agent;
import jade.core.behaviours.CyclicBehaviour;
import edu.sma.common.DfUtils;
import jade.core.Agent;
import jade.core.behaviours.*;
import jade.core.AID;
import jade.domain.DFService;
import jade.domain.FIPAAgentManagement.*;
import jade.domain.FIPAException;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.proto.AchieveREInitiator;
import java.util.*;

// espero recibir el msj de solicitud del Novio asi
// pedido turno-#turno-de-nombre-esta listo?
public class RecepcionistaAgent extends Agent{
    private Map<Integer, AID> mapaNovios = new HashMap<>(); // orden -> AID
    
    @Override
    protected void setup() {
        DfUtils.registerService(this, ServiceNames.RECEPCIONISTA_SERVICE, "consultar-service");
        System.out.println(getLocalName() + " iniciado y registrado en DF.");

        //Object[] args = getArguments();
        // TODAVIA NO IMPLEMENTO MTP ASI QUE EL PARAM 0 QUE SERA 
        //String remoteMtp = args != null && args.length > 0 ? String.valueOf(args[0]) : "http://p1-main:7778/acc";
        
        // Iniciar atención de consultas de novios
        atenderConsultas();
        
        // Iniciar escucha de notificaciones del florista
        escucharNotificacionesFlorista();
    }

    // Método para añadir un comportamiento que recibe mensajes
    protected void atenderConsultas() {

        addBehaviour(new CyclicBehaviour(this) {
            public void action() {

                MessageTemplate mt = MessageTemplate.and(
                    MessageTemplate.MatchPerformative(ACLMessage.QUERY_IF),
                    MessageTemplate.MatchProtocol("consultar-pedido")
                    );

                ACLMessage query = myAgent.receive(mt);
                if (query == null) {
                    block();
                    return;
                }

                String mensaje = query.getContent();
                String[] partes = mensaje.split("-");
                int orden = Integer.parseInt(partes[1]);
                String nombre = partes[3];
                // Guardar relación orden -> AID del novio
                mapaNovios.put(orden, query.getSender());    
                myAgent.addBehaviour(new PreguntarEstadoPedido(myAgent, query, orden, nombre));
            }
        });
    }

    private void escucharNotificacionesFlorista() {
        addBehaviour(new CyclicBehaviour() {
            public void action() {
                MessageTemplate mt = MessageTemplate.and(
                    MessageTemplate.MatchPerformative(ACLMessage.INFORM),
                    MessageTemplate.MatchSender(new AID("floristaAgent", AID.ISLOCALNAME))
                );
                ACLMessage msg = myAgent.receive(mt);
                if (msg == null) {
                    block();
                    return;
                }
                // Extraer orden del contenido "Pedido listo para orden 2"
                int orden = Integer.parseInt(msg.getContent().replaceAll("\\D+", ""));
                AID novioAID = mapaNovios.get(orden);
                if (novioAID != null) {
                    ACLMessage notificacion = new ACLMessage(ACLMessage.INFORM);
                    notificacion.addReceiver(novioAID);
                    notificacion.setContent("Tu pedido está listo");
                    myAgent.send(notificacion);
                    System.out.println("Notificado novio orden " + orden);
                }
            }
        });
    }

    private class PreguntarEstadoPedido extends AchieveREInitiator{
        private ACLMessage consultaOriginal;
        private String nombre;
        private Integer orden;

        public PreguntarEstadoPedido(Agent a, ACLMessage consultaCliente, Integer orden, String nombre) {
            super(a, crearRequest(orden)); //crea instancia de AchiveREInitiator
            this.consultaOriginal = consultaCliente;
            this.nombre = nombre;
            this.orden = orden;
        }

        private static ACLMessage crearRequest(Integer numeroOrden){
            ACLMessage request = new ACLMessage(ACLMessage.REQUEST);
            request.addReceiver(new AID("floristaAgent", AID.ISLOCALNAME));
            request.setContent("pedido-"+numeroOrden+"- esta listo?");
            request.setProtocol("consultar-pedido"); 
            return request;
        }

        @Override // Si SiguientePorAtender > numeroOrden entonces Florista responde success
        protected void handleInform(ACLMessage inform){
            String content = inform.getContent();
            if (content.contentEquals("SI")) {
                ACLMessage respuesta = consultaOriginal.createReply();
                respuesta.setPerformative(ACLMessage.INFORM);
                respuesta.setContent(nombre + ", su pedido ya esta listo");
                myAgent.send(respuesta);
                System.out.println(myAgent.getLocalName() + ": Cliente " + nombre + " notificado correctamente.");
            }
        }

        @Override
        protected void handleRefuse(ACLMessage refuse) {
            String  content = refuse.getContent();
            if (content.contentEquals("NO")) {
                ACLMessage respuesta = consultaOriginal.createReply();
                respuesta.setPerformative(ACLMessage.INFORM);
                respuesta.setContent("El pedido todavia no esta listo");
                myAgent.send(respuesta);
                System.out.println(myAgent.getLocalName() + ": Orden " + orden + " siguen en preparación");
            }
        }
        
    }

}
