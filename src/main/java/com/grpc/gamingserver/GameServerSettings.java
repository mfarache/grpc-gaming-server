package com.grpc.gamingserver;

import java.util.Arrays;
import java.util.List;

public class GameServerSettings {
	public static final String HOST = "localhost";
	public static final int PORT = 8080;
	public static final int HALL_OF_FAME_RANK = 5;
	public static final List<String> validGameNames = Arrays.asList("Mario", "Commando", "Tetris");
}
