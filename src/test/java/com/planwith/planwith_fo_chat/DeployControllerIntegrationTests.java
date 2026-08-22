package com.planwith.planwith_fo_chat;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest(properties = "eureka.client.enabled=false")
@AutoConfigureMockMvc
class DeployControllerIntegrationTests {

	@Autowired
	private MockMvc mockMvc;

	@Test
	void deployCheckReturnsMarker() throws Exception {
		mockMvc.perform(get("/api/planwith-fo-chat/deploy-check"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.service").value("planwith-fo-chat"))
				.andExpect(jsonPath("$.marker").value("planwith-fo-chat-deploy-v1"))
				.andExpect(jsonPath("$.message").value("planwith-fo-chat deploy pipeline ok"));
	}

	@Test
	void loginSucceedsWithConfiguredCredentials() throws Exception {
		mockMvc.perform(post("/api/planwith-fo-chat/login")
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{
								  "id": "test-001",
								  "pw": "1234"
								}
								"""))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.id").value("test-001"))
				.andExpect(jsonPath("$.message").value("로그인에 성공했습니다."));
	}

	@Test
	void loginFailsWithInvalidCredentials() throws Exception {
		mockMvc.perform(post("/api/planwith-fo-chat/login")
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{
								  "id": "test-001",
								  "pw": "wrong-password"
								}
								"""))
				.andExpect(status().isUnauthorized())
				.andExpect(jsonPath("$.code").value("INVALID_CREDENTIALS"));
	}

	@Test
	void loginFailsWhenRequiredValueIsBlank() throws Exception {
		mockMvc.perform(post("/api/planwith-fo-chat/login")
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{
								  "id": "",
								  "pw": "1234"
								}
								"""))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.code").value("INVALID_REQUEST"))
				.andExpect(jsonPath("$.message").value("아이디는 필수입니다."));
	}
}
