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

import java.io.IOException;
import java.util.List;

@Slf4j
@RunWith(SpringRunner.class)
@SpringBootTest
public class CartOfferPriceBoundaryTests {

	@Autowired
	private ObjectMapper objectMapper;

	@Autowired
	private RestTemplate restTemplate;

	private static final String APPLY_OFFER_URL = "http://localhost:9001/api/v1/cart/apply_offer";
	private static final String ADD_OFFER_URL = "http://localhost:9001/api/v1/offer";

	@Test
	@DisplayName("Extreme percentage value offer")
	public void testExtremePercentageOffer() throws Exception {
		OfferRequest offerRequest = new OfferRequest(2, "FLATX%", 100, List.of("p1"));
		postOffer(offerRequest);
		validateApplyOffer(new ApplyOfferRequest(100, 2, 1), 0);
	}

	@Test
	@DisplayName("Extremely large cart value")
	public void testExtremelyLargeCartValue() throws Exception {
		OfferRequest offerRequest = new OfferRequest(1, "FLATX", 20, List.of("p1"));
		postOffer(offerRequest);
		validateApplyOffer(new ApplyOfferRequest(Integer.MAX_VALUE, 1, 1), Integer.MAX_VALUE - 20);
	}

	@Test
	@DisplayName("Offer with a decimal percentage when FLAT X% coupon applied")
	public void testDecimalPercentageOffer() throws Exception {
		OfferRequest offerRequest = new OfferRequest(1, "FLATX%", (int)10.500, List.of("p1"));
		postOffer(offerRequest);
		int cartValue = 200;
		int expectedValue = cartValue - (cartValue * 10 / 100);
		validateApplyOffer(new ApplyOfferRequest(cartValue, 1, 1), expectedValue);
	}

	@Test
	@DisplayName("Zero cart value")
	public void testZeroCartValue() throws Exception {
		OfferRequest offerRequest = new OfferRequest(1, "FLATX", 20, List.of("p1"));
		postOffer(offerRequest);
		validateApplyOffer(new ApplyOfferRequest(0, 10, 1), 0);
	}

	@Test
	@DisplayName("Cart value less than offer value")
	public void testCartValueLessThanOfferValue() throws Exception {
		OfferRequest offerRequest = new OfferRequest(1, "FLATX", 30, List.of("p2"));
		postOffer(offerRequest);
		validateApplyOffer(new ApplyOfferRequest(20, 1, 2), 20);
	}

	private void postOffer(OfferRequest offerRequest) throws IOException {
		ResponseEntity<String> response = sendPostRequest(ADD_OFFER_URL, offerRequest);
		Assert.assertEquals(HttpStatus.OK, response.getStatusCode());
		log.info("Offer added successfully: {}", offerRequest);
	}

	private void validateApplyOffer(ApplyOfferRequest request, int expectedValue) throws IOException {
		ResponseEntity<ApplyOfferResponse> response = sendPostRequest(APPLY_OFFER_URL, request, ApplyOfferResponse.class);
		Assert.assertNotNull(response.getBody());
		int actualValue = response.getBody().getCart_value();
		log.info("Expected Cart Value: {}, Actual Cart Value: {}", expectedValue, actualValue);
		Assert.assertEquals(expectedValue, actualValue);
	}

	private <T> ResponseEntity<T> sendPostRequest(String url, Object requestBody, Class<T> responseType) throws IOException {
		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_JSON);
		HttpEntity<String> entity = new HttpEntity<>(objectMapper.writeValueAsString(requestBody), headers);

		try {
			return restTemplate.exchange(url, HttpMethod.POST, entity, responseType);
		} catch (Exception e) {
			log.error("Error while sending POST request to {}: {}", url, e.getMessage());
			throw new IOException("Failed to send POST request", e);
		}
	}

	private ResponseEntity<String> sendPostRequest(String url, Object requestBody) throws IOException {
		return sendPostRequest(url, requestBody, String.class);
	}

	@Configuration
	static class RestTemplateConfig {
		@Bean
		public RestTemplate restTemplate() {
			return new RestTemplate();
		}
	}

	@Configuration
	static class ObjectMapperConfig {
		@Bean
		public ObjectMapper objectMapper() {
			return new ObjectMapper();
		}
	}
}
