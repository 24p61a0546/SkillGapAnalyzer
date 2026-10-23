package com.skillgap.filter;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * AuthenticationFilter - Open access filter.
 * Login, passwords, and authentication filters have been removed.
 * All requests pass through freely without authentication barriers.
 */
public class AuthenticationFilter implements Filter {

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {}

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        // Open access: permit all requests through without requiring login credentials
        chain.doFilter(request, response);
    }

    @Override
    public void destroy() {}
}
