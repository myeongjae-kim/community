package org.kiworkshop.community.util;

import static org.assertj.core.api.BDDAssertions.then;
import static org.assertj.core.api.BDDAssertions.thenThrownBy;

import java.time.ZonedDateTime;
import java.util.Objects;
import lombok.Getter;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.io.buffer.DefaultDataBufferFactory;
import org.springframework.http.codec.json.Jackson2JsonDecoder;
import org.springframework.http.codec.json.Jackson2JsonEncoder;

@ExtendWith(MockitoExtension.class)
class JwtConverterTest {
  private static final ZonedDateTime now = ZonedDateTime.now();
  private static final String jwtSecret = "secretKeyEqualOrLargerThan32byte";

  @Getter
  static class Sample {
    String field = "field";
    ZonedDateTime zonedDateTime = now;
  }

  @Test
  void encodeAndDecode_ValidInput_ValidOutput() {
    // given
    JwtConverter<Sample> jwtService = JwtConverter.<Sample>builder()
        .clazz(Sample.class)
        .jwtSecret(jwtSecret)
        .objectMapperAsync(new ObjectMapperAsync(
            new Jackson2JsonEncoder(),
            new Jackson2JsonDecoder(),
            new DefaultDataBufferFactory()))
        .build();

    Sample sample = new Sample();

    // when
    String jwt = jwtService.encode(sample).block();
    Sample result = Objects.requireNonNull(jwtService.decode(jwt).block());

    // then
    then(result.field).isEqualTo("field");
    then(result.zonedDateTime).isEqualTo(result.zonedDateTime);
  }

  @Test
  void construct_shortJwtSecret_Exception() {
    thenThrownBy(() -> JwtConverter.<Sample>builder()
        .clazz(Sample.class)
        .jwtSecret("shortJwtSecret")
        .objectMapperAsync(new ObjectMapperAsync(
            new Jackson2JsonEncoder(),
            new Jackson2JsonDecoder(),
            new DefaultDataBufferFactory()))
        .build()
    )
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("jwtSecret's length must be large or equal than 32");
  }
}
