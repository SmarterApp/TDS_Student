package tds.student.sql.data;

import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class OpportunityStatusExtensionsTest {
  @Test
  public void shouldParseStatusString() {
    assertThat(OpportunityStatusExtensions.parse("bogus")).isNull();
    assertThat(OpportunityStatusType.parse(null)).isEqualTo(OpportunityStatusType.Unknown);
  }
}