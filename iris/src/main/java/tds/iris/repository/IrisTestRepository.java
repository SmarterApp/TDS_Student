package tds.iris.repository;

import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import TDS.Shared.Exceptions.ReturnStatusException;
import tds.student.sql.abstractions.ITestRepository;

@Component
@Scope ("prototype")
public class IrisTestRepository implements ITestRepository
{
  @Override
  public String getTrTestId (String testeeId, String testKey) throws ReturnStatusException {
    return String.format ("{Testeeid: %s,  TestKey: %s}", testeeId, testKey);
  }
}
