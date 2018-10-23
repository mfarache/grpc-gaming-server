package com.grpc.gamingserver;

import java.util.concurrent.ExecutionException;
import java.util.logging.Logger;

import com.example.gaming.Game;
import com.example.gaming.GamingServerGrpc;
import com.example.gaming.GamingServerGrpc.GamingServerBlockingStub;
import com.example.gaming.GamingServerResponse;
import com.example.gaming.HallOfFame;
import com.example.gaming.User;

import io.grpc.Channel;
import io.grpc.ManagedChannelBuilder;

public class BlockingGameServiceClient {

	private static final Logger LOG = Logger.getLogger(BlockingGameServiceClient.class.getName());

	public static void main(String[] args) throws InterruptedException, ExecutionException {
		
		Channel channel = ManagedChannelBuilder.forAddress(GameServerSettings.HOST, GameServerSettings.PORT).usePlaintext().build();
		Game game = MessageBuilder.aGame();
		User user = MessageBuilder.aUser(args[0], args[1], game);
		
		GamingServerBlockingStub blocking = blockingClient(channel);
		
		GamingServerResponse addUserResponse = blocking.addUser(user);
		LOG.info("Status:" + addUserResponse.getStatus());
		
		blocking.getUsers(game).forEachRemaining(u->LOG.info("user:" + u.getUsername()));
		
		//HallOfFame hallOfFame = blocking.getHallOfFame(MessageBuilder.aTopRequest(game, 5));
		//LOG.info("HallOfFame:" + hallOfFame.getUsersCount());

	}

	private static GamingServerBlockingStub blockingClient(Channel channel) {
		return GamingServerGrpc.newBlockingStub(channel);
	}

}
