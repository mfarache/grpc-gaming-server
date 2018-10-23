package com.grpc.gamingserver;

import java.util.concurrent.ExecutionException;
import java.util.logging.Logger;

import com.example.gaming.Game;
import com.example.gaming.GamingServerGrpc;
import com.example.gaming.GamingServerGrpc.GamingServerFutureStub;
import com.example.gaming.GamingServerResponse;
import com.example.gaming.HallOfFame;
import com.example.gaming.User;
import com.google.common.util.concurrent.ListenableFuture;

import io.grpc.Channel;
import io.grpc.ManagedChannelBuilder;

public class FutureGameServiceClient {

	private static final Logger LOG = Logger.getLogger(FutureGameServiceClient.class.getName());

	public static void main(String[] args) throws InterruptedException, ExecutionException {
		
		Channel channel = ManagedChannelBuilder.forAddress(GameServerSettings.HOST, GameServerSettings.PORT).usePlaintext().build();
		Game game = MessageBuilder.aGame();
		User user = MessageBuilder.aUser(args[0], args[1], game);

		GamingServerFutureStub futureClient = futureClient(channel);
		ListenableFuture<GamingServerResponse> futureListenerAddUser = futureClient.addUser(user);
		GamingServerResponse response = futureListenerAddUser.get();
		LOG.info("Status:" + response.getStatus());
		
		//ListenableFuture<HallOfFame> futureListenerGetHallOfFame = futureClient
		//		.getHallOfFame(MessageBuilder.aTopRequest(game, 5));
		//HallOfFame hallOfFame = futureListenerGetHallOfFame.get();
		//LOG.info("HallOfFame:" + hallOfFame.getUsersCount());

	}

	private static GamingServerFutureStub futureClient(Channel channel) {
		return GamingServerGrpc.newFutureStub(channel);
	}

}
