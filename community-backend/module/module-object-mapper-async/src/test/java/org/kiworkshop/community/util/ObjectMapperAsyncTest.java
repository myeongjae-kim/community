package org.kiworkshop.community.util;

import static org.assertj.core.api.BDDAssertions.then;

import java.nio.charset.StandardCharsets;
import java.time.ZonedDateTime;
import java.util.Objects;
import lombok.Getter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.core.io.buffer.DataBufferFactory;
import org.springframework.core.io.buffer.DefaultDataBufferFactory;
import org.springframework.http.codec.json.Jackson2JsonDecoder;
import org.springframework.http.codec.json.Jackson2JsonEncoder;
import reactor.core.publisher.Mono;

class ObjectMapperAsyncTest {
  private static final ZonedDateTime now = ZonedDateTime.now();
  private static final String serialized = "{\"field\":\"field\", \"zonedDateTime\":\"" + now.toString() + "\"}";

  @Getter
  static class Sample {
    String field = "field";
    ZonedDateTime zonedDateTime = now;
  }

  private ObjectMapperAsync objectMapperAsync;
  private DataBufferFactory dataBufferFactory;

  @BeforeEach
  void setUp() {
    dataBufferFactory = new DefaultDataBufferFactory();
    objectMapperAsync = new ObjectMapperAsync(
        new Jackson2JsonEncoder(),
        new Jackson2JsonDecoder(),
        dataBufferFactory
    );
  }

  @Test
  void writeValueAsString_ValidInput_ValidOutput() {
    Sample sample = new Sample();

    String serialized = objectMapperAsync.writeValueAsString(sample).block();
    Objects.requireNonNull(serialized);

    Sample actual = objectMapperAsync.readValue(serialized, Sample.class).block();
    Objects.requireNonNull(actual);

    then(actual.field).isEqualTo("field");
    then(actual.zonedDateTime).isEqualTo(now);
  }

  @Test
  void readValue_DataBuffer_ValidOutput() {
    Mono<DataBuffer> dataBuffer = stringBuffer(serialized);

    Sample sample = objectMapperAsync.readValue(dataBuffer, Sample.class).elementAt(0).block();
    Objects.requireNonNull(sample);

    then(sample.field).isEqualTo("field");
    then(sample.zonedDateTime).isEqualTo(now);
  }

  @Test
  void readValue_Bytes_ValidOutput() {
    Sample sample = objectMapperAsync.readValue(serialized.getBytes(), Sample.class).block();
    Objects.requireNonNull(sample);

    then(sample.field).isEqualTo("field");
    then(sample.zonedDateTime).isEqualTo(now);
  }

  @Test
  void readValue_String_ValidOutput() {
    Sample sample = objectMapperAsync.readValue(serialized, Sample.class).block();
    Objects.requireNonNull(sample);

    then(sample.field).isEqualTo("field");
    then(sample.zonedDateTime).isEqualTo(now);
  }

  private Mono<DataBuffer> stringBuffer(String value) {
    return Mono.defer(() -> {
      byte[] bytes = value.getBytes(StandardCharsets.UTF_8);
      DataBuffer buffer = dataBufferFactory.allocateBuffer();
      buffer.write(bytes);
      return Mono.just(buffer);
    });
  }
}
