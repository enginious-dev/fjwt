package it.enginious.fjwt.core;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import it.enginious.fjwt.config.FjwtDaoSourceConfig;

class FjwtDaoSourceConfigTest {

  @Test
  void defaultPasswordEncoderShouldSupportPrefixedAndLegacyBcryptHashes() {
    var passwordEncoder = new FjwtDaoSourceConfig().passwordEncoder();
    String legacyBcryptHash = new BCryptPasswordEncoder().encode("secret");

    assertThat(passwordEncoder.matches("secret", "{noop}secret")).isTrue();
    assertThat(passwordEncoder.matches("secret", legacyBcryptHash)).isTrue();
    assertThat(passwordEncoder.encode("secret")).startsWith("{bcrypt}");
  }
}
