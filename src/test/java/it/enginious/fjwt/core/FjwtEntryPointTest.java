package it.enginious.fjwt.core;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.*;

import java.io.IOException;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

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

  @Mock private HttpServletRequest httpServletRequest;

  @Captor private ArgumentCaptor<Integer> statusCodeCaptor;

  @Captor private ArgumentCaptor<String> messageCaptor;

  @Test
  void whenCommenceShouldSendError() throws IOException {

    given(httpServletRequest.getRequestURI()).willReturn("/test");
    given(httpServletRequest.getMethod()).willReturn("GET");

    doNothing().when(httpServletResponse).sendError(anyInt(), anyString());
    given(httpServletResponse.getStatus()).willReturn(200);

    target.commence(httpServletRequest, httpServletResponse, null);

    then(httpServletResponse)
        .should(times(1))
        .sendError(statusCodeCaptor.capture(), messageCaptor.capture());

    assertThat(statusCodeCaptor.getValue()).isEqualTo(HttpServletResponse.SC_UNAUTHORIZED);
    assertThat(messageCaptor.getValue()).isEqualTo("Unauthorized");
  }
}
