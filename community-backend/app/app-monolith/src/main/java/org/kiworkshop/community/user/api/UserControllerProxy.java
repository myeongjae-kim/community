package org.kiworkshop.community.user.api;

import java.util.Map;
import javax.servlet.http.HttpServletRequest;
import org.kiworkshop.community.user.api.dto.AuthenticationDto;
import org.kiworkshop.community.util.JwtConverter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.reactive.function.client.WebClient;

@RestController
@RequestMapping("/users")
public class UserControllerProxy {
  private final int maxAge;
  private final String cookieKey;
  private final boolean cookieSecure;
  private final String cookieDomain;
  private final String cookieSameSite;

  private final WebClient webClient;
  private final JwtConverter<AuthenticationDto> jwtConverter;

  public UserControllerProxy(
      @Value("${jwt.max-age}") int maxAge,
      @Value("${jwt.cookie.key}") String cookieKey,
      @Value("${jwt.cookie.secure}") boolean cookieSecure,
      @Value("${jwt.cookie.domain}") String cookieDomain,
      @Value("${jwt.cookie.same-site}") String cookieSameSite,

      WebClient.Builder webClientBuilder,
      @Value("${service.auth-api}") String authApi,
      JwtConverter<AuthenticationDto> jwtConverter
  ) {
    this.maxAge = maxAge;
    this.cookieKey = cookieKey;
    this.cookieSecure = cookieSecure;
    this.cookieDomain = cookieDomain;
    this.cookieSameSite = cookieSameSite;

    this.webClient = webClientBuilder.baseUrl(authApi).build();
    this.jwtConverter = jwtConverter;
  }

  @RequestMapping("/sign-in")
  public ResponseEntity<String> signIn(
      @RequestBody String body,
      HttpMethod method,
      @RequestHeader Map<String, String> headers,
      HttpServletRequest request
  ) {
    ResponseEntity<AuthenticationDto> authenticationDto = forward(body, method, headers, request)
        .toEntity(AuthenticationDto.class)
        .block();

    assert authenticationDto != null;

    AuthenticationDto authBody = authenticationDto.getBody();

    return ResponseEntity.ok()
        .headers(h -> {
          HttpHeaders httpHeaders = authenticationDto.getHeaders();
          h.addAll(httpHeaders);
          h.addAll(createAuthCookie(jwtConverter.encode(authBody).block()));
        })
        .body("");
  }

  private WebClient.ResponseSpec forward(
      @RequestBody String body,
      HttpMethod method,
      @RequestHeader Map<String, String> headers,
      HttpServletRequest request
  ) {
    return webClient
        .method(method)
        .uri(uri -> uri.path(request.getRequestURI()).build())
        .bodyValue(body)
        .headers(h -> h.setAll(headers))
        .retrieve();
  }

  private HttpHeaders createAuthCookie(String jwt) {
    HttpHeaders headers = new HttpHeaders();
    headers.add(HttpHeaders.SET_COOKIE, cookieKey
        + "="
        + jwt
        + "; Max-Age=" + maxAge
        + "; Domain=" + cookieDomain
        + "; Path=/"
        + (cookieSecure ? "; Secure" : "")
        + "; HttpOnly; SameSite=" + cookieSameSite);

    return headers;
  }
}
