package it.enginious.fjwt.core;

import org.junit.jupiter.api.Test;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.assertj.core.api.Assertions.assertThat;

class FjwtDummyUserDetailsServiceTest {

    @Test
    void testLoadUserByUsername() {
        FjwtDummyUserDetailsService target =
                new FjwtDummyUserDetailsService(
                        new PasswordEncoder() {
                            @Override
                            public String encode(CharSequence rawPassword) {
                                return rawPassword.toString();
                            }

                            @Override
                            public boolean matches(CharSequence rawPassword, String encodedPassword) {
                                return false;
                            }
                        });
        UserDetails user = target.loadUserByUsername("username");
        assertThat(user.getUsername()).isEqualTo("username");
        assertThat(user.getPassword()).isEqualTo("username");
        assertThat(user.getAuthorities()).isEmpty();
    }
}
