**Borramos los compilados previos**
Remove-Item -Recurse -Force out
javac -d out -cp "jade.jar" src/main/java/edu/sma/agents/*.java src/main/java/edu/sma/common/*.java
**Ejecutamos la versión básica (1 plataforma), tovaia no esta implementado Docker**
java -cp ".;jade.jar;out" jade.Boot -gui -agents "novio1:edu.sma.agents.NovioAgent(1,Matias);novio2:edu.sma.agents.NovioAgent(2,Alejandro);novio3:edu.sma.agents.NovioAgent(3,Luigi);recepcionistaAgent:edu.sma.agents.RecepcionistaAgent;floristaAgent:edu.sma.agents.FloristaAgent"
