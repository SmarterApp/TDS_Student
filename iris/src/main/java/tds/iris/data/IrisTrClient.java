package tds.iris.data;

import org.opentestsystem.shared.trapi.ITrClient;
import org.opentestsystem.shared.trapi.TrApiContentType;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;

public class IrisTrClient implements ITrClient
{

  @Override
  public String getPackage (String url) {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public String getForObject (String url) {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public <T> T getForObject (String url, Class<T> responseType) {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public String getForObject (String url, TrApiContentType contentType) {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public <T> T getForObject (String url, TrApiContentType contentType, Class<T> responseType) {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public <T> T postForObject (String url, Object request, Class<T> responseType) {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public void put (String url, Object request) {
    // TODO Auto-generated method stub

  }

  @Override
  public ResponseEntity<String> exchange (String url, String requestBody, TrApiContentType contentType, HttpMethod httpMethod) {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public <T> ResponseEntity<T> exchange (String url, String requestBody, TrApiContentType contentType, HttpMethod httpMethod, Class<T> responseType) {
    // TODO Auto-generated method stub
    return null;
  }

}
