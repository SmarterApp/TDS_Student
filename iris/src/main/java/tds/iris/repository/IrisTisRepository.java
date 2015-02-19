package tds.iris.repository;

import java.util.UUID;

import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import TDS.Shared.Exceptions.ReturnStatusException;
import tds.student.sql.abstractions.ITisRepository;

@Component
@Scope ("prototype")
public class IrisTisRepository implements ITisRepository
{

  @Override
  public void tisReply (UUID oppkey, Boolean success, String errorMessage) throws ReturnStatusException {
    // Do nothing.
  }

}
