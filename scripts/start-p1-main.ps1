mvn -DskipTests package
java --add-opens=java.xml/com.sun.org.apache.xerces.internal.jaxp=ALL-UNNAMED -cp "target/sma-jade-distribuido-1.0.0.jar;target/lib/*;jade.jar" jade.Boot -name P1 -gui -local-host localhost -local-port 1099 -mtp jade.mtp.http.MessageTransportProtocol(http://localhost:7778/acc) restaurante:edu.sma.agents.RestaurantAgent
