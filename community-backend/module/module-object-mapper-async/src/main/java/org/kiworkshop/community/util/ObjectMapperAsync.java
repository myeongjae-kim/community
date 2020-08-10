package org.kiworkshop.community.util;

import java.nio.charset.Charset;
import org.reactivestreams.Publisher;
import org.springframework.core.ResolvableType;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.core.io.buffer.DataBufferFactory;
import org.springframework.http.codec.json.Jackson2JsonDecoder;
import org.springframework.http.codec.json.Jackson2JsonEncoder;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public class ObjectMapperAsync {

  private final Jackson2JsonEncoder encoder;
  private final Jackson2JsonDecoder decoder;
  private final DataBufferFactory dataBufferFactory;

  public ObjectMapperAsync(
      Jackson2JsonEncoder encoder,
      Jackson2JsonDecoder decoder,
      DataBufferFactory dataBufferFactory
  ) {
    this.encoder = encoder;
    this.decoder = decoder;
    this.dataBufferFactory = dataBufferFactory;
  }

  public Mono<String> writeValueAsString(Object object) {
    Flux<DataBuffer> result = encoder.encode(
        Mono.just(object),
        dataBufferFactory,
        ResolvableType.forClass(String.class),
        null,
        null);

    return result
        .reduce(new StringBuilder(), this::dataBufferToStringBuilder)
        .map(StringBuilder::toString);
  }

  public <T> Flux<T> readValue(Publisher<DataBuffer> dataBuffer, Class<T> valueType) {
    return decoder
        .decode(dataBuffer, ResolvableType.forClass(valueType), null, null)
        .cast(valueType);
  }

  public <T> Mono<T> readValue(byte[] bytes, Class<T> valueType) {
    // readValue의 첫 번째 매개변수로 Mono가 들어가기 때문에 리턴값 Flux의 element는 1개입니다.
    return readValue(byteBuffer(bytes), valueType).elementAt(0);
  }

  public <T> Mono<T> readValue(String string, Class<T> valueType) {
    return readValue(byteBuffer(string.getBytes()), valueType).elementAt(0);
  }

  private StringBuilder dataBufferToStringBuilder(StringBuilder stringBuilder, DataBuffer dataBuffer) {
    return stringBuilder.append(dataBuffer.toString(Charset.defaultCharset()));
  }

  private Mono<DataBuffer> byteBuffer(byte[] bytes) {
    return Mono.defer(() -> {
      DataBuffer buffer = dataBufferFactory.allocateBuffer();
      buffer.write(bytes);
      return Mono.just(buffer);
    });
  }
}

