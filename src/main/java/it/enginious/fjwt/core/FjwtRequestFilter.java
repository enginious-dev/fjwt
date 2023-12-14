package it.enginious.fjwt.core;

import io.jsonwebtoken.JwtException;
import it.enginious.fjwt.core.exceptions.FjwtTokenInvalidatorException;
import jakarta.annotation.PostConstruct;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Check if a token is supplied in request and its validity. If a valid token is found the
 * authentication is added in the {@link org.springframework.security.core.context.SecurityContext}.
 *
 * @author Giuseppe Milazzo
 * @since 1.0.0
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class FjwtRequestFilter extends OncePerRequestFilter {

    private static final String AUTHORIZATION_HEADER = "Authorization";
    private static final Pattern TOKEN_PATTERN = Pattern.compile("^Bearer (.+)$");
    private static final int TOKEN_GROUP = 1;

    private final FjwtTokenUtil fjwtTokenUtil;
    private final FjwtTokenInvalidator fjwtTokenInvalidator;
    private final FjwtConfig fjwtConfig;
    private List<AntPathRequestMatcher> unsecuredEndpointsMatchers;

    /**
     * initialize this bean, see {@link PostConstruct}
     */
    @PostConstruct
    public void init() {
        unsecuredEndpointsMatchers =
                Arrays.stream(fjwtConfig.getAllUnsecuredEndpoints())
                        .map(AntPathRequestMatcher::new)
                        .toList();
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {

        if (unsecuredEndpointsMatchers.stream().anyMatch(rm -> rm.matches(request))) {
            log.debug(
                    "request is for [{}] which is an unsecured endpoint, bypassing chain",
                    request.getPathInfo());
        } else {
            handleRequest(request);
        }

        log.debug("invoking chain");
        chain.doFilter(request, response);
    }

    private void handleRequest(HttpServletRequest request) {
        log.debug("retrieving token from request using header [{}]", AUTHORIZATION_HEADER);
        String requestTokenHeader = request.getHeader(AUTHORIZATION_HEADER);
        Matcher matcher =
                TOKEN_PATTERN.matcher(StringUtils.defaultIfBlank(requestTokenHeader, StringUtils.EMPTY));

        if (matcher.matches()) {
            log.debug("token matched with pattern [{}]", TOKEN_PATTERN.pattern());
            UserDetails userDetails = null;
            String jwtToken = StringUtils.trim(matcher.group(TOKEN_GROUP));

            try {
                userDetails = fjwtTokenUtil.getUserFromToken(jwtToken);
            } catch (JwtException | IllegalArgumentException e) {
                logger.warn("exception occurred while parsing token: ", e);
            }

            if (Objects.nonNull(userDetails)
                    && Objects.isNull(SecurityContextHolder.getContext().getAuthentication())) {

                if (!wasTokenInvalidated(userDetails, jwtToken)) {
                    log.debug("building authentication for user [{}]", userDetails.getUsername());
                    UsernamePasswordAuthenticationToken authentication =
                            new UsernamePasswordAuthenticationToken(
                                    userDetails, null, userDetails.getAuthorities());
                    authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    log.debug(
                            "adding authentication for user [{}] to security context", userDetails.getUsername());
                    SecurityContextHolder.getContext().setAuthentication(authentication);
                } else {
                    log.debug("token was invalidated for user [{}]", userDetails.getUsername());
                }
            }
        } else {
            log.debug("token did not match with pattern [{}]", TOKEN_PATTERN.pattern());
        }
    }

    private boolean wasTokenInvalidated(UserDetails userDetails, String jwtToken) {

        try {
            return !(fjwtTokenInvalidator instanceof NoopTokenInvalidator)
                    && fjwtTokenInvalidator.wasInvalidated(userDetails, jwtToken);
        } catch (FjwtTokenInvalidatorException e) {
            log.error(
                    String.format(
                            "error while checking if token was invalidated for user [%s]:",
                            userDetails.getUsername()),
                    e);
            return Boolean.TRUE;
        }
    }
}
