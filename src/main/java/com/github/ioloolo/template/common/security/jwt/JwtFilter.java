package com.github.ioloolo.template.common.security.jwt;

import com.auth0.jwt.exceptions.TokenExpiredException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.ioloolo.template.common.exception.AccessTokenExpiredException;
import com.github.ioloolo.template.common.exception.AuthenticationFailException;
import com.github.ioloolo.template.common.security.authentication.UserAuthentication;
import com.github.ioloolo.template.domain.api.dto.response.ApiResponse;
import com.github.ioloolo.template.domain.api.exception.ApiException;
import com.github.ioloolo.template.domain.user.repository.UserRepository;
import com.github.ioloolo.template.domain.user.schema.User;
import jakarta.annotation.Nonnull;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class JwtFilter extends OncePerRequestFilter {

	private final JwtUtil jwtUtil;
	private final UserRepository repository;

	@Override
	protected void doFilterInternal(
			@Nonnull HttpServletRequest request,
			@Nonnull HttpServletResponse response,
			@Nonnull FilterChain filterChain
	) throws ServletException, IOException {

		String accessToken = extractToken(request);

		try {
			if (accessToken != null && jwtUtil.validateToken(accessToken)) {
				String id = jwtUtil.extractToken(accessToken);
				User user = repository.findById(id)
						.orElseThrow(AuthenticationFailException::new);

				UserAuthentication authentication = new UserAuthentication(user);
				authentication.setAuthenticated(true);

				SecurityContextHolder.getContext().setAuthentication(authentication);
			}
		} catch (TokenExpiredException e) {
			handleException(response, new AccessTokenExpiredException());
			return;
		} catch (ApiException e) {
			handleException(response, e);
			return;
		}

		filterChain.doFilter(request, response);
	}

	private String extractToken(HttpServletRequest request) {
		String authorization = request.getHeader("Authorization");

		return (authorization != null && authorization.startsWith("Bearer "))
				? authorization.substring(7)
				: null;
	}

	private void handleException(HttpServletResponse response, ApiException e) throws IOException {
		ApiResponse<?> apiResponse = ApiResponse.createError(e);
		String apiResponseString = new ObjectMapper().writeValueAsString(apiResponse);

		response.getWriter().write(apiResponseString);
		response.getWriter().flush();
	}
}