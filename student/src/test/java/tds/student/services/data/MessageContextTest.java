package tds.student.services.data;

import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;


public class MessageContextTest {
  @Test
  public void shouldHaveBuilderForMessageContext() {
    MessageContext context = MessageContext.builder().setContext("context").build();
    assertThat(context.getContext()).isEqualTo("context");
  }
}