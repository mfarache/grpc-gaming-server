package com.grpc.gamingserver.server;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import com.example.gaming.Game;
import com.example.gaming.GamingServerGrpc.GamingServerImplBase;
import com.example.gaming.GamingServerResponse;
import com.example.gaming.HallOfFame;
import com.example.gaming.Score;
import com.example.gaming.TopNHallOfFameRequest;
import com.example.gaming.User;
import com.grpc.gamingserver.GameServerSettings;
import com.grpc.gamingserver.MessageBuilder;

import io.grpc.stub.StreamObserver;

public class GameService extends GamingServerImplBase {

	private static final Logger LOG = Logger.getLogger(GameService.class.getName());
	// for each game we may have multiple users
	private Map<Game, List<User>> usersPerGame = new HashMap<>();
	// for each game we may have multiple scores, even from the same user
	private Map<Game, List<Score>> scorePerGame = new HashMap<>();

	private List<StreamObserver<HallOfFame>> hallOfFameSubscribers = new ArrayList<>();

	public GameService() {
		GameServerSettings.validGameNames.stream().forEach(game -> {
			usersPerGame.put(MessageBuilder.aGame(game), new ArrayList<>());
			scorePerGame.put(MessageBuilder.aGame(game), new ArrayList<>());
		});
	}

	@Override
	public StreamObserver<Score> addScore(StreamObserver<GamingServerResponse> responseObserver) {

		LOG.info("--- AddScore request");

		return new StreamObserver<Score>() {
			@Override
			public void onCompleted() {
				LOG.info("End of stream");
			}

			@Override
			public void onError(Throwable arg0) {
				LOG.info("ERROR" + arg0);
			}

			@Override
			public void onNext(Score score) {
				LOG.info(String.format("Score: %s %s", score.getUsername(), score.getPoints()));
				Game game = score.getGame();
				if (scorePerGame.containsKey(game)) {
					scorePerGame.get(game).add(score);
					hallOfFameSubscribers.forEach(subscriber -> {
						LOG.info("About to notify: " + subscriber.toString());
						notifyConsumer(subscriber, calculateTopScores(game, GameServerSettings.HALL_OF_FAME_RANK));
					});
					responseObserver.onNext(MessageBuilder.okResponse());

				} else {
					responseObserver.onNext(MessageBuilder.errorResponse());
					responseObserver.onError(new IllegalArgumentException("Inexisting Game:" + game.getName()));
				}
				responseObserver.onCompleted();
			}
		};
	}

	@Override
	public void addUser(User request, StreamObserver<GamingServerResponse> responseObserver) {
		LOG.info("--- addUser request");
		Game game = request.getGame();
		if (usersPerGame.containsKey(game)) {
			usersPerGame.get(game).add(request);
			responseObserver.onNext(MessageBuilder.okResponse());
		} else {
			responseObserver.onNext(MessageBuilder.errorResponse());
			responseObserver.onError(new IllegalArgumentException("Inexisting Game:" + game.getName()));
		}

		responseObserver.onCompleted();
	}

	@Override
	public void getHallOfFame(TopNHallOfFameRequest request, StreamObserver<HallOfFame> responseObserver) {
		LOG.info("--- getHallOfFame request");
		Game game = request.getGame();

		if (scorePerGame.containsKey(game)) {
			hallOfFameSubscribers.add(responseObserver);
		} else {
			responseObserver.onError(new IllegalArgumentException("Inexisting Game:" + game.getName()));
		}
		// We do not want to close the stream as we are interested on sending updates to
		// all our consumers when they submit scores.
		// responseObserver.onCompleted();
	}

	@Override
	public void getUsers(Game request, StreamObserver<User> responseObserver) {
		LOG.info("--- getUsers request");
		usersPerGame.get(request).forEach(
				user -> responseObserver.onNext(MessageBuilder.aUser(user.getEmail(), user.getUsername(), request)));

		responseObserver.onCompleted();
	}

	private List<Score> calculateTopScores(Game game, int n) {
		List<Score> scores = scorePerGame.get(game);
		LOG.info("--- scores received:" + scores.size());

		final List<Score> topScores = scores.stream()
				.sorted((o1, o2) -> Integer.valueOf(o2.getPoints()).compareTo(Integer.valueOf(o1.getPoints())))
				.limit(n).collect(Collectors.toList());
		
		//scores.sort(Comparator.comparing(o -> o.getItem().getValue()));

		// List<Score> topScores = scores.stream().sorted(comparing(s->s.)
		// .limit(n).collect(toList());
		topScores.forEach(s -> {
			LOG.info("--- r:" + s.getPoints());
		});
		LOG.info("--- hallOfFame:" + topScores.size());
		return topScores;
	}

	private void notifyConsumer(StreamObserver<HallOfFame> responseObserver, List<Score> topScores) {
		responseObserver.onNext(HallOfFame.newBuilder().addAllUsers(topScores).build());
	}

}
