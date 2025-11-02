package gismp;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

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
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		Assertions.assertEquals(200, statusCode);
	}
	
}
