package org.jadaptive.box.niofs.api.client;

import com.box.sdk.BoxAPIConnection;
import org.jadaptive.box.niofs.api.auth.session.AuthenticatedSessionFactory;
import org.jadaptive.box.niofs.api.auth.session.type.AuthenticatedSessionType;

import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Function;

public class BoxAPIClient {
	
	private final BoxAPIConnection connection;
	
	public BoxAPIClient(AuthenticatedSessionType authenticatedSessionType, Map<String, String> properties) {
		var session = AuthenticatedSessionFactory.get(authenticatedSessionType, properties);
		this.connection = session.getBoxAPIConnection();
	}
	
	public <R> R doInBox(Function<BoxAPIConnection, R> function) {
		return function.apply(this.connection);
	}
	
	public void doInBox(Consumer<BoxAPIConnection> function) {
		function.accept(this.connection);
	}

}
