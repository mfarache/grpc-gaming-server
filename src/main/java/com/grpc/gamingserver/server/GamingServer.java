package com.grpc.gamingserver.server;

import java.io.IOException;
import java.util.logging.Logger;

import com.grpc.gamingserver.GameServerSettings;

import io.grpc.Server;
import io.grpc.ServerBuilder;

public class GamingServer {
	
	private static final Logger LOG = Logger.getLogger(GamingServer.class.getName());

	private Server server;

	public static void main(String[] args) throws IOException, InterruptedException {
		final GamingServer server = new GamingServer();
		server.start();
		server.blockUntilShutdown();
	}

	private void start() throws IOException {
		ServerBuilder serverBuilder = ServerBuilder.forPort(GameServerSettings.PORT).addService(new GameService());
		server = serverBuilder.build().start();
		LOG.info("Server started, listening on " + GameServerSettings.PORT);

		Runtime.getRuntime().addShutdownHook(new Thread() {
			@Override
			public void run() {
				GamingServer.this.stop();
			}
		});
	}

	private void stop() {
		if (server != null) {
			System.err.println("*** shutting down gRPC server since JVM is shutting down");
			server.shutdown();
			System.err.println("*** server shut down");
		}
	}

	private void blockUntilShutdown() throws InterruptedException {
		if (server != null) {
			server.awaitTermination();
		}
	}

}


