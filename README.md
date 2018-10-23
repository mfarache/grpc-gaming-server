# grpc-gaming-server

mvn clean install

#start the gaming server
mvn exec:java -Dexec.mainClass="com.grpc.gamingserver.server.GamingServer" 

#start the async client with user user1. Will add a user into the server and add a score
mvn exec:java -Dexec.mainClass="com.grpc.gamingserver.AsyncGameServiceClient" -Dexec.args="user1 user1@gmail.com" 

