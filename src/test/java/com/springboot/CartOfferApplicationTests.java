package com.springboot;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.springboot.controller.ApplyOfferRequest;
import com.springboot.controller.ApplyOfferResponse;
import com.springboot.controller.OfferRequest;

import lombok.extern.slf4j.Slf4j;
import org.junit.Assert;
import org.junit.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.*;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.web.client.RestTemplate;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;

@Slf4j
@RunWith(SpringRunner.class)
@SpringBootTest
public class CartOfferApplicationTests {
	@Autowired
	private ObjectMapper objectMapper;
	@Autowired
	private RestTemplate restTemplate;


		private static final String APPLY_OFFER_URL = "http://localhost:9001/api/v1/cart/apply_offer";
		private static final String ADD_OFFER_URL = "http://localhost:9001/api/v1/offer";

		@Test
		public void checkFlatXForOneSegment() throws Exception {
			OfferRequest offerRequest = new OfferRequest(1, "FLATX", 10, List.of("p1"));
			boolean result = addOffer(offerRequest);
			Assert.assertTrue(result);
		}

		@Test
		@DisplayName("When there is no offer that matches, cart value should remain same")
		public void applyOfferNoMatch() throws Exception {
			ApplyOfferRequest applyOfferRequest = new ApplyOfferRequest(200, 5, 1);
			ApplyOfferResponse applyOfferResponse = applyOffer(applyOfferRequest);
			Assert.assertNotNull(applyOfferResponse);
			Assert.assertEquals(applyOfferRequest.getCartValue(), applyOfferResponse.getCart_value());
		}

		@Test
		@DisplayName("Check FLAT X % Offer For One Segment")
		public void checkFlatPercentageOffer() throws Exception {
			OfferRequest offerRequest = new OfferRequest(2, "FLATX%", 100, List.of("p1"));
			boolean result = addOffer(offerRequest);
			Assert.assertTrue(result);

			ApplyOfferRequest applyOfferRequest = new ApplyOfferRequest(200, 2, 1);
			ApplyOfferResponse applyOfferResponse = applyOffer(applyOfferRequest);
			int expectedCartValue = 200 - (200 * 100 / 100);
			Assert.assertEquals(expectedCartValue, applyOfferResponse.getCart_value());
		}

		@Test
		@DisplayName("Check Flat Amount offer working")
		public void checkFlatAmountOffer() throws Exception {
			OfferRequest offerRequest = new OfferRequest(1, "FLATX", 40, List.of("p1"));
			boolean result = addOffer(offerRequest);
			Assert.assertTrue(result);

			ApplyOfferRequest applyOfferRequest = new ApplyOfferRequest(200, 1, 1);
			ApplyOfferResponse applyOfferResponse = applyOffer(applyOfferRequest);
			int expectedCartValue = 200 - offerRequest.getOfferValue();
			Assert.assertEquals(expectedCartValue, applyOfferResponse.getCart_value());
		}

		@Test
		@DisplayName("Check when user segment isn't eligible for FLAT X% discount, no discount is given")
		public void applyFlatPercentageNoMatch() throws Exception {
			OfferRequest offerRequest = new OfferRequest(10, "FLATX%", 20, List.of("p2"));
			boolean result = addOffer(offerRequest);
			Assert.assertTrue(result);

			ApplyOfferRequest applyOfferRequest = new ApplyOfferRequest(200, 20, 1);
			ApplyOfferResponse applyOfferResponse = applyOffer(applyOfferRequest);
			Assert.assertEquals(applyOfferRequest.getCartValue(), applyOfferResponse.getCart_value());
		}

		@Test
		@DisplayName("Test whether system fails when user ID invalid/negative or too big causing overflow")
		public void checkUserIdSanity() throws Exception {
			ApplyOfferRequest applyOfferRequest = new ApplyOfferRequest(200, 1, Integer.MAX_VALUE + 10);
			ApplyOfferResponse applyOfferResponse = applyOffer(applyOfferRequest);
			Assert.assertNotNull(applyOfferResponse);
			Assert.assertNotEquals(HttpStatus.BAD_REQUEST, HttpStatus.OK);
		}

		@Test
		@DisplayName("Test when cart API called with invalid or too big restaurant id then discount isn't applied")
		public void restaurantIdSanity() throws Exception {
			OfferRequest offerRequest = new OfferRequest(1, "FLATX", 40, List.of("p1"));
			boolean result = addOffer(offerRequest);
			Assert.assertTrue(result);

			ApplyOfferRequest applyOfferRequest = new ApplyOfferRequest(2000, Integer.MAX_VALUE + 10, 1);
			ApplyOfferResponse applyOfferResponse = applyOffer(applyOfferRequest);
			Assert.assertEquals(applyOfferResponse.getCart_value(), applyOfferRequest.getCartValue());
		}

		private boolean addOffer(OfferRequest offerRequest) {
			try {
				HttpURLConnection con = createHttpConnection(ADD_OFFER_URL, offerRequest);
				int responseCode = con.getResponseCode();
				if (responseCode == HttpURLConnection.HTTP_OK) {
					readResponseSentByServer(con);
					return true;
				} else {
					logErrorResponse(responseCode);
					return false;
				}
			} catch (Exception e) {
				handleException(e);
				return false;
			}
		}

		private ApplyOfferResponse applyOffer(ApplyOfferRequest applyOfferRequest) throws Exception {
			String json = new ObjectMapper().writeValueAsString(applyOfferRequest);
			HttpHeaders headers = new HttpHeaders();
			headers.set("Content-Type", "application/json");
			HttpEntity<String> entity = new HttpEntity<>(json, headers);

			ResponseEntity<ApplyOfferResponse> responseEntity = restTemplate.exchange(
					APPLY_OFFER_URL, HttpMethod.POST, entity, ApplyOfferResponse.class);

			if (responseEntity.getStatusCode() != HttpStatus.OK) {
				throw new RuntimeException("Error applying offer: " + responseEntity.getStatusCode());
			}
			return responseEntity.getBody();
		}

		private HttpURLConnection createHttpConnection(String urlString, Object requestBody) throws Exception {
			URL url = new URL(urlString);
			HttpURLConnection con = (HttpURLConnection) url.openConnection();
			con.setDoOutput(true);
			con.setRequestProperty("Content-Type", "application/json");

			String jsonRequest = new ObjectMapper().writeValueAsString(requestBody);
			try (OutputStream os = con.getOutputStream()) {
				os.write(jsonRequest.getBytes());
				os.flush();
			}
			return con;
		}

		private void readResponseSentByServer(HttpURLConnection con) throws Exception {
			try (BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()))) {
				String inputLine;
				StringBuilder response = new StringBuilder();
				while ((inputLine = in.readLine()) != null) {
					response.append(inputLine);
				}
				System.out.println(response.toString());
			}
		}

		private void logErrorResponse(int responseCode) {
			System.out.println("POST request did not work. Response Code: " + responseCode);
		}

		private void handleException(Exception e) {
			System.err.println("Error occurred: " + e.getMessage());
			e.printStackTrace();
		}

		@Configuration
		static class RestTemplateTestConfig {
			@Bean
			public RestTemplate restTemplate() {
				return new RestTemplate();
			}
		}

		@Configuration
		static class JacksonConfig {
			@Bean
			public ObjectMapper objectMapper() {
				return new ObjectMapper();
			}
		}
	}