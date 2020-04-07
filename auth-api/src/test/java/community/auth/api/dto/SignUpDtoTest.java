package community.auth.api.dto;

import community.auth.model.Social;
import org.springframework.test.util.ReflectionTestUtils;

public class SignUpDtoTest {
  public static SignUpDto getSignUpDtoFixture() {
    SignUpDto signUpDto = new SignUpDto();
    ReflectionTestUtils.setField(signUpDto, "provider", Social.Provider.GOOGLE);
    ReflectionTestUtils.setField(signUpDto, "providerAccessToken", "providerAccessToken");

    return signUpDto;
  }
}
