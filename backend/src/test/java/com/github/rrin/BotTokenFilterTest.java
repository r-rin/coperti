package com.github.rrin;

import com.github.rrin.telegram.security.BotTokenFilter;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

class BotTokenFilterTest {

    private MockFilterChain run(BotTokenFilter filter, String header, MockHttpServletResponse response) throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/bot/me");
        if (header != null) request.addHeader(BotTokenFilter.TOKEN_HEADER, header);
        MockFilterChain chain = new MockFilterChain();
        filter.doFilter(request, response, chain);
        return chain;
    }

    @Test
    void correctTokenPasses() throws Exception {
        MockHttpServletResponse response = new MockHttpServletResponse();
        assertNotNull(run(new BotTokenFilter("s3cret"), "s3cret", response).getRequest());
        assertEquals(200, response.getStatus());
    }

    @Test
    void wrongOrMissingTokenIsUnauthorized() throws Exception {
        for (String header : new String[]{"nope", null, ""}) {
            MockHttpServletResponse response = new MockHttpServletResponse();
            assertNull(run(new BotTokenFilter("s3cret"), header, response).getRequest());
            assertEquals(401, response.getStatus());
        }
    }

    @Test
    void unconfiguredServerRejectsEverything() throws Exception {
        MockHttpServletResponse response = new MockHttpServletResponse();
        assertNull(run(new BotTokenFilter("  "), "", response).getRequest());
        assertEquals(503, response.getStatus());
    }
}
