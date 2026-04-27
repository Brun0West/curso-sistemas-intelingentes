import jade.core.AID;
import jade.core.behaviours.CyclicBehaviour;
import jade.core.behaviours.TickerBehaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.proto.AchieveREInitiator;
import jade.core.Agent;

// ------------------------------------------------------------
// Clase con constantes para los mensajes
// ------------------------------------------------------------
interface Mensajes {
    String QUERY_SALUDO = "saludoDisponible?";
    String REQUEST_OBTENER_SALUDO = "dameSaludo";
    String SALUDO_EXITO = "¡Hola, colega!";
    String SALUDO_FALLIDO = "Lo siento, no puedo saludar ahora";
}

// ------------------------------------------------------------
// Agente B: el que recibe el REQUEST y decide el camino
// ------------------------------------------------------------
class AgenteB extends Agent {
    private boolean puedoResponder = false; // Simula condición variable

    protected void setup() {
        // Cambiar la variable después de unos segundos (simulación)
        addBehaviour(new TickerBehaviour(this, 5000) {
            protected void onTick() { // Duda: que otros metodos existen ademas de on tick sobretodo hablame de los que contempla cyclic behaviour
                puedoResponder = !puedoResponder;
                System.out.println(myAgent.getLocalName() + ": ahora puedoResponder = " + puedoResponder);
            }
        });

        // Comportamiento para atender REQUEST
        addBehaviour(new CyclicBehaviour() {
            public void action() {
                MessageTemplate mt = MessageTemplate.MatchPerformative(ACLMessage.REQUEST);
                ACLMessage msg = myAgent.receive(mt);
                if (msg != null) {
                    if (msg.getContent().equals(Mensajes.REQUEST_OBTENER_SALUDO)) {
                        ACLMessage reply = msg.createReply();
                        if (puedoResponder) {
                            reply.setPerformative(ACLMessage.INFORM);
                            reply.setContent(Mensajes.SALUDO_EXITO);
                        } else {
                            reply.setPerformative(ACLMessage.REFUSE);
                            reply.setContent(Mensajes.SALUDO_FALLIDO);
                        }
                        myAgent.send(reply);
                    }
                } else {
                    block();
                    return;
                }
            }
        });
    }
}

// ------------------------------------------------------------
// Agente A: recibe QUERY-IF y lanza REQUEST a Agente B
// ------------------------------------------------------------
class AgenteA extends Agent {

    protected void setup() {
        // Comportamiento para recibir QUERY-IF
        addBehaviour(new CyclicBehaviour() {
            public void action() {
                MessageTemplate mt = MessageTemplate.MatchPerformative(ACLMessage.QUERY_IF); // permite filtrar por tipo de performativa
                ACLMessage query = myAgent.receive(mt);
                if (query != null) { 
                    if (query.getContent().equals(Mensajes.QUERY_SALUDO)) {
                        // Lanzamos un comportamiento que ejecutará el REQUEST a AgenteB
                        myAgent.addBehaviour(new ConsultarSaludoBehaviour(myAgent, query));
                    } else {
                        // Respuesta por defecto para otras consultas
                        ACLMessage reply = query.createReply();
                        reply.setPerformative(ACLMessage.REFUSE);
                        reply.setContent("Consulta no reconocida");
                        myAgent.send(reply); // DUDA: como el msj sabe a quien se le tiene que devolver la respuesta? o es que es info que vino en la query procesada en la linea (K)
                    }
                } else {
                    block();;
                    return;
                }
            }
        });
    }

    // Comportamiento especializado para manejar el diálogo REQUEST con AgenteB
    private class ConsultarSaludoBehaviour extends AchieveREInitiator {
        private ACLMessage consultaOriginal; // Mensaje original del cliente

        public ConsultarSaludoBehaviour(Agent a, ACLMessage consultaCliente) {
            super(a, crearRequest()); //recibe un agente y acl message, es decir que instancia un AchieveREInitiator
            this.consultaOriginal = consultaCliente;
        }

        // Construye el mensaje REQUEST que se enviará a AgenteB
        private static ACLMessage crearRequest() {
            ACLMessage request = new ACLMessage(ACLMessage.REQUEST); //creo un obj usando el tipo de performativa
            request.addReceiver(new AID("agenteB", AID.ISLOCALNAME)); // para que funcione necesito lanzar al agente como -agents agenteB:AgenteB
            request.setContent(Mensajes.REQUEST_OBTENER_SALUDO);
            request.setProtocol("saludo-protocol"); // que logra esta linea?
            return request; // retornarlo es necesario para que AchieveREInitiator funcione correctamente y me habilite los caminos 1 y2
        }

        // CAMINO 1: AgenteB respondió con INFORM (éxito)
        @Override
        protected void handleInform(ACLMessage inform) {
            String saludo = inform.getContent();
            // Construir respuesta exitosa al cliente original
            ACLMessage respuesta = consultaOriginal.createReply();
            respuesta.setPerformative(ACLMessage.INFORM);
            respuesta.setContent("El saludo es: " + saludo);
            myAgent.send(respuesta);
            System.out.println(myAgent.getLocalName() + ": Respondí con INFORM - todo bien.");
        }

        // CAMINO 2: AgenteB respondió con REFUSE (fallo)
        @Override
        protected void handleRefuse(ACLMessage refuse) {
            String motivo = refuse.getContent();
            ACLMessage respuesta = consultaOriginal.createReply();
            respuesta.setPerformative(ACLMessage.INFORM); // por qué no se uso aqui un refuse directamente? 
            respuesta.setContent("No se pudo obtener el saludo: " + motivo);
            myAgent.send(respuesta);
            System.out.println(myAgent.getLocalName() + ": Respondí con INFORM (negativo) debido a REFUSE.");
        }

        // Manejo de fallo (timeout, error de comunicación, etc.)
        @Override
        protected void handleFailure(ACLMessage failure) {
            ACLMessage respuesta = consultaOriginal.createReply();
            respuesta.setPerformative(ACLMessage.FAILURE);
            respuesta.setContent("Error al contactar al servicio de saludos");
            myAgent.send(respuesta);
            System.out.println(myAgent.getLocalName() + ": Falló la comunicación con AgenteB.");
        }
    }
}

// ------------------------------------------------------------
// Agente cliente para probar el QUERY-IF
// ------------------------------------------------------------
class Cliente extends Agent {
    protected void setup() {
        // Enviar QUERY-IF a AgenteA
        ACLMessage query = new ACLMessage(ACLMessage.QUERY_IF);
        query.addReceiver(new AID("agenteA", AID.ISLOCALNAME));
        query.setContent(Mensajes.QUERY_SALUDO);
        send(query); // envia el mensaje al agenteA
        // Esperar respuesta
        addBehaviour(new CyclicBehaviour() {
            public void action() {
                ACLMessage response = receive();
                if (response != null) {
                    System.out.println("Cliente recibió: " + response.getContent());
                    myAgent.doDelete();
                } else {
                    block();
                    return;
                }
            }
        });
    }
}