package gismp;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpRequest.BodyPublisher;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import tools.jackson.databind.ObjectMapper;

/**
 * This utility allows creating documents for
 * new products at GIS MP service. It is thread-safe and
 * protected from overloading the server with too many requests
 * using a custom Semaphore solution. <br>
 * It is not a full-release working solution, but a solid basis to go from.
 * @see DocumentDescription
 * @see DocumentProduct
 * @see RequestDocument
 * @see RequestBody
 * @see TimedSemaphore
 */
public class CrptApi {
	private final TimedSemaphore timedSemaphore;
	private final ObjectMapper objectMapper;
	
	// All protected fields may be modified through inheritance
	protected HttpClient httpClient;
	protected String url = "https://ismp.crpt.ru/api/v3/lk/documents/create";
	
	/** 
	 * This request body may be accessed before sending a request,
	 * some its parameters may be changed if ever needed.
	 */
	protected volatile RequestBody request;
	
	/**
	 * Creates an instance of CrptApi utility with
	 * the given configuration.
	 * @param timeUnit TimeUnit used to prevent the server from being overloaded.
	 * @param requestLimit Number of requests per said TimeUnit.
	 * @see TimedSemaphore
	 */
	public CrptApi(TimeUnit timeUnit, int requestLimit) {
		timedSemaphore = new TimedSemaphore(timeUnit, requestLimit);
		objectMapper = new ObjectMapper();
		request = new RequestBody();
		this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(10))
                .version(HttpClient.Version.HTTP_2)
                .build();
	}
	
	/**
	 * A dummy method that pretends to return an access token. <br>
	 * Must be re-implemented be the real, 
	 * production ready instance of the utility. <br>
	 * Optional arguments may be used for various purposes 
	 * (we can pass a filename or a java.util.Properties object, 
	 * for example)
	 * @param args May be used in order to prevent the access to the token
	 * from some malware code.
	 * @return String that contains the authentication token.
	 */
	protected String getAuthToken(Object... args) {
		return "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiIxMjM0NTY3ODkwIiwibmFtZSI6IkpvaG4gRG9lIiwiaWF0IjoxNTE2MjM5MDIyfQ.SflKxwRJSMeKKF2QT4fwpMeJf36POk6yJV_adQssw5c";
	}
	
	/**
	 * Makes a POST request, telling the server to create a document
	 * for the new product.
	 * @param document The document to be posted.
	 * @param signature The signature of the user 
	 * (required by the server).
	 * @return CompletableFuture containing the HTTP response, 
	 * with its body as plain text.
	 * @throws InterruptedException If the semaphore failed 
	 * for some reason.
	 * @see RequestDocument
	 */
	public CompletableFuture<HttpResponse<String>> createDocument(
			RequestDocument document, 
			String signature
			) throws InterruptedException {
		String fullUrl = null;
		BodyPublisher bodyPublisher = null;
		
		// Try to acquire a permit
		timedSemaphore.acquire();
		
		// Update the document in the body (thread-safely)
		synchronized (request) {
			request.productDocument = document;

			// Create the publisher for request
			String jsonBody = objectMapper
					.writeValueAsString(request);
			bodyPublisher = HttpRequest.BodyPublishers
					.ofByteArray(jsonBody.getBytes());
			// Set the group for the product
			fullUrl = String.format("%s?pg=%s", url, request.pg);
		}
        
		// Build request
        HttpRequest httpRequest = HttpRequest.newBuilder()
                .uri(URI.create(fullUrl))
                .header("content-type", "application/json")
                .header("Authorization", "Bearer " + getAuthToken())
                .POST(bodyPublisher)
                .timeout(Duration.ofSeconds(30))
                .build();
        
        // Send request asynchronously
        CompletableFuture<HttpResponse<String>> completableFuture = null;
        completableFuture = CompletableFuture.supplyAsync(() -> {
        	try {
				return httpClient.send(
				        httpRequest, HttpResponse.BodyHandlers.ofString());
			} catch (IOException | InterruptedException e) {
				return null;
			}
        });
		return completableFuture;
	}
	
	
	/**
	 * Additional information on the product document.
	 * <br>
	 * Part of the RequestDocument (product document).
	 * @see RequestDocument
	 */
	public static class DocumentDescription {
		@JsonProperty("participant_inn")
		String participantInn = "sample";
	}
	
	/**
	 * One of the products included into
	 * the product document. <br>
	 * Part of the RequestDocument.
	 * @see RequestDocument
	 */
	public static class DocumentProduct {
		@JsonProperty("certificate_document")
		String certificateDocument = "sample";
		@JsonProperty("certificate_document_date")
		String certificateDocumentDate = "2025-11-02";
		@JsonProperty("certificate_document_number")
		String certificateDocumentNumber = "sample";
		@JsonProperty("owner_inn")
		String ownerInn = "sample";
		@JsonProperty("producer_inn")
		String producerInn = "sample";
		@JsonProperty("production_date")
		String productionDate = "2025-11-02";
		@JsonProperty("tnved_code")
		String tnvedCode = "sample";
		@JsonProperty("uit_code")
		String uitCode = "sample";
		@JsonProperty("uitu_code")
		String uituCode = "sample";
	}
	
	/**
	 * Data object that contains all the information on
	 * the new product. <br>
	 * Part of the RequestBody. <br>
	 * Must include one or more actual products relative to
	 * this document (DocumentProduct).
	 * @see RequestBody
	 * @see DocumentProduct
	 * @see DocumentDescription
	 */
	public static class RequestDocument {
		@JsonProperty("doc_id")
		String docId = "sample";
		@JsonProperty("doc_status")
		String docStatus = "sample";
		@JsonProperty("doc_type")
		String docType = "sample";
		@JsonProperty("owner_inn")
		String ownerInn = "sample";
		@JsonProperty("participant_inn")
		String participantInn = "sample";
		@JsonProperty("producer_inn")
		String producerInn = "sample";
		@JsonProperty("production_date")
		String productionDate = "2025-11-02";
		@JsonProperty("production_type")
		String productionType = "sample";
		@JsonProperty("reg_date")
		String regDate = "2025-11-02";
		@JsonProperty("reg_number")
		String regNumber = "sample";
		
		@JsonProperty("importRequest")
		boolean importRequest = true;
		
		@JsonProperty("description")
		DocumentDescription description = new DocumentDescription();
		@JsonProperty("products")
		List<DocumentProduct> products = new ArrayList<>();
	}
	
	/**
	 * The main request body to be sent to the server. <br>
	 * Contains all the required information, including
	 * the product document (RequestDocument).
	 * @see RequestDocument
	 */
	public static class RequestBody {
		@JsonIgnore
		public String pg = "milk";

		@JsonProperty("document_format")
		String documentFormat = "MANUAL";
		@JsonProperty("type")
		String type = "LP_INTRODUCE_GOODS";

		@JsonProperty("product_document")
		RequestDocument productDocument = new RequestDocument();
		@JsonProperty("signature")
		String signature = "Sample signature";
		
	}
	
	
	/**
	 * A special version of Java Semaphore. <br>
	 * This kind of Semaphore has only {@code acquire()} method,
	 * that reduces the number of available permits, and blocks
	 * the caller-Thread (scheduling the execution for later)
	 * if no permits available. <br>
	 * The permits number is automatically replenished on a timed basis,
	 * using the daemon-thread for this purpose.
	 */
	public static class TimedSemaphore implements Runnable {
		final TimeUnit timeUnit;
		final int requestLimit;
		
		private final Semaphore semaphore;
		private final Thread releaseThread;
		
		/**
		 * @see TimedSemaphore
		 * @param timeUnit Specifies a TimeUnit for 
		 * releasing a Semaphore permit. <br>
		 * The {@code timeout} value 
		 * of the Unit (for Thread sleeping) will always be set to 1,
		 * so only the TimeUnit itself (seconds, minutes, etc.) matters.
		 * @param requestLimit Maximum number of available
		 * request during the mentioned TimeUnit.
		 */
		public TimedSemaphore(TimeUnit timeUnit, int requestLimit) {
			this.timeUnit = timeUnit;
			this.requestLimit = requestLimit;
			semaphore = new Semaphore(requestLimit, true);
			releaseThread = new Thread(this);
			releaseThread.setDaemon(true);
			releaseThread.start();
		}
		
		/**
		 * Acquires a permit from this semaphore,
		 * locking the Thread and scheduling it for a later
		 * execution if no permits available. <br>
		 * The permits are replenished automatically on a timed basis.
		 * @throws InterruptedException If the current Thread was interrupted
		 * while waiting for a permit.
		 */
		public void acquire() throws InterruptedException {
			semaphore.acquire();
		}

		@Override
		public void run() {
			while (!Thread.currentThread().isInterrupted()) {
				try {
					// Release semaphore on a timed basis
					timeUnit.sleep(1);
					
					int currentPermits = semaphore.availablePermits();
					if (currentPermits < requestLimit) {
						semaphore.release();
					}
				} catch (InterruptedException e) {
					Thread.currentThread().interrupt();
				}
			}
		}
	}
	
}
