package com.enginious.fjwt.core;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.anyInt;
import static org.mockito.BDDMockito.anyString;
import static org.mockito.BDDMockito.doNothing;
import static org.mockito.BDDMockito.then;
import static org.mockito.BDDMockito.times;

import java.io.IOException;
import javax.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class FjwtEntryPointTest {

  private final FjwtEntryPoint target = new FjwtEntryPoint();

  @Mock private HttpServletResponse httpServletResponse;

  @Captor private ArgumentCaptor<Integer> statusCodeCaptor;

  @Captor private ArgumentCaptor<String> messageCaptor;

  @Test
  void whenCommenceShouldSendError() throws IOException {

    doNothing().when(httpServletResponse).sendError(anyInt(), anyString());

    target.commence(null, httpServletResponse, null);

    then(httpServletResponse)
        .should(times(1))
        .sendError(statusCodeCaptor.capture(), messageCaptor.capture());

    assertThat(statusCodeCaptor.getValue()).isEqualTo(HttpServletResponse.SC_UNAUTHORIZED);
    assertThat(messageCaptor.getValue()).isEqualTo("Unauthorized");
  }
}
