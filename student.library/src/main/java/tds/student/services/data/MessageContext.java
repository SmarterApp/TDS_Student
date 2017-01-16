package tds.student.services.data;

import com.google.auto.value.AutoValue;

import tds.common.ValidationError;

@AutoValue
public abstract class MessageContext {
  public abstract ValidationError getError();

  public abstract String getContext();

  public abstract String getClientName();

  public abstract String getLanguage();

  public abstract String getAppKey();

  static Builder builder() {
    return new AutoValue_MessageContext.Builder();
  }

  @AutoValue.Builder
  public abstract static class Builder {
    public abstract Builder setError(ValidationError value);

    public abstract Builder setContext(String context);

    public abstract Builder setClientName(String clientName);

    public abstract Builder setLanguage(String language);

    public abstract Builder setAppKey(String appKey);

    public abstract MessageContext build();
  }
}
