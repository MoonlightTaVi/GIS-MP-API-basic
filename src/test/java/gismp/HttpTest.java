package gismp;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import gismp.CrptApi.RequestDocument;

/**
 * This test makes sure the HttpClient
 * is working properly, as well as the server responds.
 */
public class HttpTest {

	/**
	 * Make a test GET request that creates
	 * a temporary access token (which, in turn,
	 * does nothing by itself and expires very quickly)
	 */
	@Test
	public void connectionEstablished() {
		int statusCode = 0;
		
		HttpClient httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(10))
                .version(HttpClient.Version.HTTP_2)
                .build();
		HttpRequest httpRequest = HttpRequest.newBuilder()
				.uri(URI.create("https://ismp.crpt.ru/api/v3/auth/cert/key"))
				.GET()
				.timeout(Duration.ofSeconds(8))
				.build();
		
		try {
			HttpResponse<String> httpResponse = httpClient.send(
					httpRequest, 
					HttpResponse.BodyHandlers.ofString()
					);
			statusCode = httpResponse.statusCode();
		} catch (IOException | InterruptedException e) {
			System.err.printf("Exception happened: %s%n", e.getLocalizedMessage());
		}
		
		Assertions.assertEquals(200, statusCode);
	}
	
	@Test
	public void accessDenied() {
		CrptApi api = new CrptApi(TimeUnit.SECONDS, 1);
		HttpResponse<String> response = null;
		try {
			response = api.createDocument(
					new RequestDocument(), "no-signature"
					).get();
			int statusCode = response.statusCode();
			Assertions.assertEquals(403, statusCode);
			Assertions.assertEquals(
					"Billing contract not signed by participant.",
					response.body()
					);
		} catch (InterruptedException | ExecutionException e) {
			System.err.printf("Exception happened: %s%n", e.getLocalizedMessage());
		}
	}
	
}
