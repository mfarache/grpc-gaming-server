package com.grpc.gamingserver;

import java.util.concurrent.ExecutionException;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.example.gaming.Game;
import com.example.gaming.GamingServerGrpc;
import com.example.gaming.GamingServerGrpc.GamingServerStub;
import com.example.gaming.GamingServerResponse;
import com.example.gaming.HallOfFame;
import com.example.gaming.Score;
import com.example.gaming.User;

import io.grpc.Channel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.stub.StreamObserver;

public class AsyncGameServiceClient {

	private static final Logger LOG = Logger.getLogger(AsyncGameServiceClient.class.getName());
	private static GamingServerStub async;

	public static void main(String[] args) throws InterruptedException, ExecutionException {

		Channel channel = ManagedChannelBuilder.forAddress(GameServerSettings.HOST, GameServerSettings.PORT).usePlaintext().maxInboundMessageSize(16*1024*1024).build();
		Game game = MessageBuilder.aGame();
		User user = MessageBuilder.aUser(args[0], args[1], game);
		
		async = asyncClient(channel);

		async.addUser(user, new StreamObserver<GamingServerResponse>() {
			@Override
			public void onCompleted() {
				LOG.info("Call to addUser was completed");
			}

			@Override
			public void onError(Throwable arg0) {
				LOG.log(Level.WARNING, "addUser:" + arg0.getMessage(), arg0);
			}

			@Override
			public void onNext(GamingServerResponse arg0) {
				LOG.info("addUser - Status:" + arg0.getStatus());
				
			}
		});

		Thread.sleep(1000);
		getUsers(game);
		
		Thread.sleep(1000);
		hallOfFame(GameServerSettings.HALL_OF_FAME_RANK, game);

		Thread.sleep(1000);
		LOG.info("**** Add Score 1");
		addScore(user.getUsername(), 1000, game);

		Thread.sleep(100000);		

	}
	
	private static void addScore(String user, int points, Game game) {
		LOG.info("-- addScore");
		StreamObserver<Score> scoreObserver = async.addScore(new StreamObserver<GamingServerResponse>() {
			@Override
			public void onCompleted() {
				LOG.info(game.getName() + ": Call to addScore was completed user:" + user );
			}

			@Override
			public void onError(Throwable arg0) {
				LOG.log(Level.WARNING, "addScore:" + arg0.getMessage(), arg0);
			}

			@Override
			public void onNext(GamingServerResponse response) {
				LOG.info("addScore - Status:" + response.getStatus());
			}
		});
		scoreObserver.onNext(MessageBuilder.aScore(points, user, game));
	}
	
	private static void hallOfFame(int n, Game game) {
		LOG.info("-- hallOfFame");
		async.getHallOfFame(MessageBuilder.aTopRequest(game, n), new StreamObserver<HallOfFame>() {
			@Override
			public void onCompleted() {
				LOG.info(game.getName() + ": Call to getHallOfFame was completed");
			}

			@Override
			public void onError(Throwable arg0) {
				LOG.log(Level.WARNING, "getHallOfFame:" + arg0.getMessage(), arg0);
			}

			@Override
			public void onNext(HallOfFame hallOfFame) {
				hallOfFame.getUsersList().stream().forEach(u->{
					LOG.info("Hall of fame: Score:" + u.getUsername() + " score:" + u.getPoints());
				});
			}
		});
	}
	
	private static void getUsers(Game game) {
		LOG.info("-- getUsers");
		async.getUsers(game, new StreamObserver<User>() {
			@Override
			public void onCompleted() {
				LOG.info(game.getName() + ": Call to getUsers was completed");
			}

			@Override
			public void onError(Throwable arg0) {
				LOG.log(Level.WARNING, "getUsers:" + arg0.getMessage(), arg0);
			}

			@Override
			public void onNext(User user) {
				LOG.info("Existing User:" + user.getUsername());
			}
		});
	}

	private static GamingServerStub asyncClient(Channel channel) {
		return GamingServerGrpc.newStub(channel);
	}

}
