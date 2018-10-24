package com.grpc.gamingserver.server;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.io.IOException;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.mockito.ArgumentMatcher;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import com.example.gaming.Game;
import com.example.gaming.GamingServerGrpc;
import com.example.gaming.GamingServerGrpc.GamingServerBlockingStub;
import com.example.gaming.GamingServerResponse;
import com.example.gaming.GamingServerResponse.StatusType;
import com.example.gaming.User;

import io.grpc.ManagedChannel;
import io.grpc.inprocess.InProcessChannelBuilder;
import io.grpc.inprocess.InProcessServerBuilder;
import io.grpc.stub.StreamObserver;
import io.grpc.testing.GrpcCleanupRule;

@RunWith(JUnit4.class)
public class GamingServerTest {

	private static final String USER = "user";
	private static final String USER_GMAIL_COM = "user@gmail.com";

	@Mock
	GameService gameService;

	@Rule
	public GrpcCleanupRule grpcCleanup = new GrpcCleanupRule();

	private String serverName;
	private InProcessServerBuilder serverBuilder;
	private InProcessChannelBuilder channelBuilder;

	@Before
	public void init() {
		MockitoAnnotations.initMocks(this);
		serverName = InProcessServerBuilder.generateName();
		serverBuilder = InProcessServerBuilder.forName(serverName).directExecutor();
		channelBuilder = InProcessChannelBuilder.forName(serverName).directExecutor();
		registerServiceAndStart();
	}

	@Test
	public void clientCallShouldTriigerAddUserInTheServer() {
		GamingServerBlockingStub blockingClient = GamingServerGrpc.newBlockingStub(getChannel());
		GamingServerResponse response = blockingClient.addUser(aUser(aGame()));
		assertEquals(StatusType.OK, response.getStatus());

		Mockito.verify(gameService).addUser(Mockito.argThat(new ArgumentMatcher<User>() {
			@Override
			public boolean matches(Object argument) {
				User user = (User) argument;
				return user.getEmail().equals(USER_GMAIL_COM) && user.getUsername().equals(USER);
			}
		}), Mockito.argThat(new ArgumentMatcher<StreamObserver<GamingServerResponse>>() {
			@Override
			public boolean matches(Object argument) {
				return true;
			}
		}));
	}

	private ManagedChannel getChannel() {
		return grpcCleanup.register(channelBuilder.build());
	}

	private void registerServiceAndStart() {
		try {
			grpcCleanup.register(serverBuilder.addService(gameService).build().start());
		} catch (IOException e) {
			fail();
		}
	}

	private User aUser(Game game) {
		return User.newBuilder().setUsername(USER).setEmail(USER_GMAIL_COM).setGame(game).build();
	}

	private Game aGame() {
		Game game = Game.newBuilder().setName("Game").build();
		return game;
	}
}
