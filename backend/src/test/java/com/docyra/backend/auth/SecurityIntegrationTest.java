package com.docyra.backend.auth;

import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.regex.Matcher;
import java.util.regex.Pattern;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

@SpringBootTest
class SecurityIntegrationTest {

    private static final String GOOD_LOGIN = """
            {"email":"secretariat@docyra.local","password":"change-me"}""";
    private static final String WRONG_PASSWORD = """
            {"email":"secretariat@docyra.local","password":"mauvais"}""";

    @Autowired
    private WebApplicationContext context;

    private MockMvc mvc;

    @BeforeEach
    void setUp() {
        mvc = MockMvcBuilders.webAppContextSetup(context).apply(springSecurity()).build();
    }

    @Test
    void protectedRouteWithoutTokenIsRejected() throws Exception {
        mvc.perform(get("/api/patients"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void loginWithGoodCredentialsReturnsToken() throws Exception {
        mvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON).content(GOOD_LOGIN))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").isNotEmpty())
                .andExpect(jsonPath("$.expiresAt").isNotEmpty());
    }

    @Test
    void loginWithWrongPasswordIsRejected() throws Exception {
        mvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON).content(WRONG_PASSWORD))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.detail").value("Email ou mot de passe incorrect"));
    }

    @Test
    void protectedRouteWithTokenIsAllowed() throws Exception {
        String body = mvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON).content(GOOD_LOGIN))
                .andReturn().getResponse().getContentAsString();
        Matcher matcher = Pattern.compile("\"token\":\"([^\"]+)\"").matcher(body);
        matcher.find();

        mvc.perform(get("/api/patients").header("Authorization", "Bearer " + matcher.group(1)))
                .andExpect(status().isOk());
    }
        private String scanToken() throws Exception {
        String login = mvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON).content(GOOD_LOGIN))
                .andReturn().getResponse().getContentAsString();
        String jwt = extractToken(login);
        String scan = mvc.perform(post("/api/scan-sessions").header("Authorization", "Bearer " + jwt))
                .andReturn().getResponse().getContentAsString();
        return extractToken(scan);
    }

    private String extractToken(String json) {
        Matcher matcher = Pattern.compile("\"token\":\"([^\"]+)\"").matcher(json);
        matcher.find();
        return matcher.group(1);
    }

    @Test
    void scanTokenCanSendDocuments() throws Exception {
        var notAnImage = new MockMultipartFile("file", "notes.txt", "text/plain", new byte[] {1});

        mvc.perform(multipart("/api/documents").file(notAnImage).header("X-Scan-Token", scanToken()))
                .andExpect(status().isBadRequest());
    }

    @Test
    void scanTokenCannotReadPatients() throws Exception {
        mvc.perform(get("/api/patients").header("X-Scan-Token", scanToken()))
                .andExpect(status().isForbidden());
    }

    @Test
    void invalidScanTokenIsRejected() throws Exception {
        mvc.perform(get("/api/patients").header("X-Scan-Token", "faux"))
                .andExpect(status().isUnauthorized());
    }
}