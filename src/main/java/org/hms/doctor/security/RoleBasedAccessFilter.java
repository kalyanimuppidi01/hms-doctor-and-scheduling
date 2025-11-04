package org.hms.doctor.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;
import java.util.Set;

@Component
@Profile("!dev")
public class RoleBasedAccessFilter extends OncePerRequestFilter {

    // Paths that should be publicly accessible (no RBAC)
    private static final List<String> PUBLIC_PATHS = List.of(
            "/v3/api-docs",           // OpenAPI json
            "/v3/api-docs/",
            "/swagger-ui.html",
            "/swagger-ui",
            "/swagger-ui/index.html",
            "/swagger-ui/",
            "/swagger-ui/**",
            "/swagger-resources",
            "/actuator/health",
            "/actuator/info",
            "/favicon.ico",
            "/healthcheck/ready",
            "/healthcheck/live"
    );

    private boolean isPublic(String path) {
        if (path == null) return false;
        // exact match or prefix match for swagger-ui and api-docs variants
        if (path.startsWith("/v3/api-docs")) return true;
        if (path.startsWith("/swagger-ui") || path.startsWith("/swagger-resources")) return true;
        if (path.startsWith("/actuator")) return path.equals("/actuator/health") || path.equals("/actuator/info");
        if (path.equals("/favicon.ico")) return true;
        if (path.equals("/healthcheck/ready")) return path.equals("/healthcheck/ready");
        if (path.equals("/healthcheck/live")) return path.equals("/healthcheck/live");
        return false;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {

        String path = request.getRequestURI();

        // Allow Swagger / actuator / OPTIONS without checks
        if ("OPTIONS".equalsIgnoreCase(request.getMethod()) || isPublic(path)) {
            filterChain.doFilter(request, response);
            return;
        }

        // 🔹 First try to get role from authenticated principal
        String role = null;
        var auth = org.springframework.security.core.context.SecurityContextHolder
                .getContext().getAuthentication();

        if (auth != null && auth.isAuthenticated() && auth.getAuthorities() != null) {
            for (var ga : auth.getAuthorities()) {
                String authority = ga.getAuthority();   // e.g. "ROLE_ADMIN"
                if (authority.startsWith("ROLE_")) {
                    role = authority.substring(5).toLowerCase(); // admin
                } else {
                    role = authority.toLowerCase();
                }
                break; // take first
            }
        }

        // 🔹 If no authenticated role, fallback to header
        if (role == null) {
            role = request.getHeader("X-User-Role");
        }
        if (role == null) role = "anonymous";

        String method = request.getMethod();
        boolean allowed = checkPermission(role, method, path);

        if (!allowed) {
            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
            response.setContentType("application/json");
            response.getWriter().write("""
            {"code":"FORBIDDEN","message":"access denied for role","correlationId":"-"}
        """);
            return;
        }

        filterChain.doFilter(request, response);
    }


    private boolean checkPermission(String role, String method, String path) {
        if ("admin".equalsIgnoreCase(role)) return true;

        if ("doctor".equalsIgnoreCase(role)) {
            // doctors can only GET
            return HttpMethod.GET.matches(method);
        }

        if ("reception".equalsIgnoreCase(role)) {
            // reception allowed: GET, POST, PUT (not DELETE)
            if (HttpMethod.DELETE.matches(method)) return false;
            return Set.of("GET","POST","PUT").contains(method);
        }

        // anonymous: allow only safe GETs to /v1/patients/*/exists
        if ("anonymous".equalsIgnoreCase(role)) {
            if (HttpMethod.GET.matches(method) && path.matches("^/v1/patients/\\d+/exists$")) return true;
            return false;
        }

        // default deny
        return false;
    }
}
