package org.kiworkshop.community.util;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.interfaces.JWTVerifier;
import lombok.Builder;
import org.springframework.util.Assert;
import reactor.core.publisher.Mono;

public class JwtConverter<T> {
  private static final int JWT_SECRET_MIN_LENGTH = 32;

  private final Class<T> clazz;
  private final ObjectMapperAsync objectMapperAsync;

  private final Algorithm algorithm;
  private final JWTVerifier jwtVerifier;

  @Builder
  private JwtConverter(Class<T> clazz, String jwtSecret, ObjectMapperAsync objectMapperAsync) {
    Assert.isTrue(jwtSecret.getBytes().length >= JWT_SECRET_MIN_LENGTH,
        "jwtSecret's length must be large or equal than " + JWT_SECRET_MIN_LENGTH);

    this.clazz = clazz;
    this.objectMapperAsync = objectMapperAsync;
    this.algorithm = Algorithm.HMAC256(jwtSecret);
    this.jwtVerifier = JWT.require(this.algorithm).build();
  }

  public Mono<String> encode(T subject) {
    return objectMapperAsync.writeValueAsString(subject)
        .map(subjectAlter -> JWT.create().withSubject(subjectAlter).sign(algorithm));
  }

  public Mono<T> decode(String jwt) {
    String serializedSubject = jwtVerifier.verify(jwt).getSubject();

    return objectMapperAsync.readValue(serializedSubject, clazz);
  }
}
