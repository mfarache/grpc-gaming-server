package com.grpc.gamingserver;

import java.util.Random;

import com.example.gaming.Game;
import com.example.gaming.GamingServerResponse;
import com.example.gaming.Score;
import com.example.gaming.TopNHallOfFameRequest;
import com.example.gaming.User;
import com.example.gaming.GamingServerResponse.StatusType;

public class MessageBuilder {
	public static User aUser(String username, String email, Game game) {
		return User.newBuilder().setEmail(email).setUsername(username).setGame(game).build();
	}
	
	public static Score aScore(int points, String username, Game game) {
		return Score.newBuilder().setPoints(points).setUsername(username).setGame(game).build();
	}
	
	public static Game aGame() {
		return Game.newBuilder().setName(GameServerSettings.validGameNames.get(new Random().nextInt(3))).build();
	}
	
	public static Game aGame(String gameName) {
		return Game.newBuilder().setName(gameName).build();
	}
	
	public static TopNHallOfFameRequest aTopRequest(Game game, int n) {
		return TopNHallOfFameRequest.newBuilder().setHowMan(n).setGame(game).build();
	}
	
	public static GamingServerResponse okResponse() {
		return GamingServerResponse.newBuilder().setStatus(StatusType.OK).build();
	}
	
	public static GamingServerResponse errorResponse() {
		return GamingServerResponse.newBuilder().setStatus(StatusType.ERR).build();
	}

}
