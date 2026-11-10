package com.shopverse.app.api;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.*;
import static org.junit.jupiter.api.Assertions.*;
class ApiRateLimitFilterTest {
    @Test void rejectsExcessAndLeavesStaticResourcesAccessible() throws Exception {
        var filter=new ApiRateLimitFilter();
        for(int i=0;i<100;i++) {
            var response=new MockHttpServletResponse();
            filter.doFilter(new MockHttpServletRequest("GET","/api/home"),response,(a,b)->{});
            assertEquals(200,response.getStatus());
        }
        var blocked=new MockHttpServletResponse();
        filter.doFilter(new MockHttpServletRequest("POST","/api/orders/direct"),blocked,(a,b)->fail("must not reach order handler"));
        assertEquals(429,blocked.getStatus());
        assertEquals("1",blocked.getHeader("Retry-After"));
        var staticResponse=new MockHttpServletResponse();
        filter.doFilter(new MockHttpServletRequest("GET","/index.html"),staticResponse,(a,b)->{});
        assertEquals(200,staticResponse.getStatus());
    }
}
