package it.enginious.fjwt.core;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatNoException;

class NoopTokenInvalidatorTest {

    private final NoopTokenInvalidator target = new NoopTokenInvalidator();

    @Test
    void whenInvokingStoreNoExceptionShouldBeThrown() {
        assertThatNoException().isThrownBy(() -> target.store(null, null));
    }

    @Test
    void whenInvokingWasInvalidatedShouldReturnFalse() {
        assertThat(target.wasInvalidated(null, null)).isFalse();
    }
}
