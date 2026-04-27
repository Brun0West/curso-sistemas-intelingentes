package edu.sma.agents;
import jade.domain.DFService;
import jade.domain.FIPAException;
import jade.lang.acl.ACLMessage;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.core.AID;
import jade.core.Agent;
import edu.sma.common.ServiceNames;
import jade.core.behaviours.CyclicBehaviour;
import jade.core.behaviours.TickerBehaviour;

public class Novio extends Agent{
    int turno;
    String nombre;
    AID recepcionista;
    public void setup(){

        Object[] args = getArguments();
        this.turno = args != null && args.length > 0 ? Integer.parseInt(args[0].toString()) : 1; 
        this.nombre = args != null && args.length > 1 ? String.valueOf(args[1]) : "Luis";

        recepcionista = contactarRecepcionista();
        if (recepcionista == null) {
            System.err.println("No se pudo encontrar al recepcionista. El agente terminará.");
            doDelete();
            return;
        }
        
        // Ignora: String remoteMtp = args != null && args.length > 2 ? String.valueOf(args[2]) : "http://p1-main:7778/acc";  para cuando se implemente MTP

        // Preguntar cada 1000*(turno+5) ms por pedido
        addBehaviour(new TickerBehaviour(this, (turno+5)*1000) {
            protected void onTick() { // Duda: que otros metodos existen ademas de on tick sobretodo hablame de los que contempla cyclic behaviour
                 ACLMessage query = new ACLMessage(ACLMessage.QUERY_IF); // DUDA: cuando importaba "import jade.domain.introspection.ACLMessage" en lugar de "import jade.lang.acl.ACLMessage" tenia error, explicame la diferencia entre ambos archivos
                //Definimos el protocolo del tipo de consulta, esto ayuda a filtrar mejor los mensajes
                query.setProtocol("consultar-pedido");
                if(recepcionista !=null){
                    query.addReceiver(recepcionista); 
                } else{
                    System.out.println("Ha ocurrido un error");
                }
                String msj = "pedido turno-" + turno + "-de-" + nombre + "-esta listo?";

                query.setContent(msj);
                myAgent.send(query);
            }
        });

        // Esperamos la respuesta
        addBehaviour(new CyclicBehaviour() {
            public void action() {
                ACLMessage response = receive();
                if (response == null) {
                    block();
                    return;
                } 
                if (response.getContent().contains("no esta listo")) {
                    System.err.println("Entiendo, seguire esperando.");
                }else{
                    ACLMessage msg = new ACLMessage(ACLMessage.CONFIRM);
                    msg.addReceiver(recepcionista);
                    msg.setContent("Gracias, ya estoy yendo a recogerlo.");
                    System.err.println("SISTEMA: Cliente" + nombre + "ha terminado su comunciación.");
                    myAgent.doDelete();
                }
            }
        });
    }

    private AID contactarRecepcionista(){ // por que devuelvo un AID en lugar de un string?
        DFAgentDescription modelo = new DFAgentDescription();
        ServiceDescription sd = new ServiceDescription();
        sd.setType(ServiceNames.RECEPCIONISTA_SERVICE);
        modelo.addServices(sd);
        try{
            DFAgentDescription[] results = DFService.search(this, modelo);
            if(results.length > 0){
                return results[0].getName();
            }
            } catch (FIPAException e) {
            e.printStackTrace();
        }
        return null;
    }
}


/* Definicion de la clase AID
public class AID implements Comparable, Serializable {
   public static final char HAP_SEPARATOR = '@';
   private static String platformID;
   private String name;
   private int hashCode;
   private static final int EXPECTED_ADDRESSES_SIZE = 1;
   private static final int EXPECTED_RESOLVERS_SIZE = 1;
   private List addresses;
   private List resolvers;
   private Properties userDefSlots;
   public static final boolean ISGUID = true;
   public static final boolean ISLOCALNAME = false;
   public static final String AGENT_CLASSNAME = "JADE-agent-classname";
   private transient Long persistentID;
    ...
 */