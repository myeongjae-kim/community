package org.kiworkshop.community.config;

import org.kiworkshop.community.user.api.dto.AuthenticationDto;
import org.kiworkshop.community.util.JwtConverter;
import org.kiworkshop.community.util.ObjectMapperAsync;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.buffer.DefaultDataBufferFactory;
import org.springframework.http.codec.json.Jackson2JsonDecoder;
import org.springframework.http.codec.json.Jackson2JsonEncoder;

@Configuration
public class BeanConfig {

  @Bean
  public JwtConverter<AuthenticationDto> jwtConverter(
      @Value("${jwt.secret}") String jwtSecret
  ) {
    return JwtConverter.<AuthenticationDto>builder()
        .clazz(AuthenticationDto.class)
        .jwtSecret(jwtSecret)
        .objectMapperAsync(new ObjectMapperAsync(
            new Jackson2JsonEncoder(),
            new Jackson2JsonDecoder(),
            new DefaultDataBufferFactory()))
        .build();
  }
}
