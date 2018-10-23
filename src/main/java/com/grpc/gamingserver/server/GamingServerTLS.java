package com.grpc.gamingserver.server;

import java.io.IOException;
import java.net.URI;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.logging.Logger;

import com.grpc.gamingserver.GameServerSettings;

import io.grpc.Server;
import io.grpc.ServerBuilder;

public class GamingServerTLS {
	
	private static final Logger LOG = Logger.getLogger(GamingServerTLS.class.getName());

	private Server server;

	/**
	 * Main launches the server from the command line.
	 */
	public static void main(String[] args) throws IOException, InterruptedException {
		final GamingServerTLS server = new GamingServerTLS();
		server.start();
		server.blockUntilShutdown();
	}

	private void start() throws IOException {
		
		ServerBuilder serverBuilder = ServerBuilder.forPort(GameServerSettings.PORT).addService(new GameService());
		addTLSSupport(serverBuilder);
		
		server = serverBuilder.build().start();
		LOG.info("Server started, listening on " + GameServerSettings.PORT);

		Runtime.getRuntime().addShutdownHook(new Thread() {
			@Override
			public void run() {

				GamingServerTLS.this.stop();

			}
		});
	}
	
	private void addTLSSupport(ServerBuilder serverBuilder ) {
		String basePath = "<your-workspace>/grpc-gaming-server/src/main/resources";
		final Path certChainFile = Paths.get(URI.create(basePath+"/cert.pem"));
		final Path privateKeyFile = Paths.get(URI.create(basePath+"/key.pem"));
		if (certChainFile != null && privateKeyFile != null) {
			LOG.info("Starting TLS" );
			serverBuilder.useTransportSecurity(certChainFile.toFile(), privateKeyFile.toFile());
        }
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


