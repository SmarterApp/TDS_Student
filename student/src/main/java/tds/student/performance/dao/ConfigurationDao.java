package tds.student.performance.dao;

import tds.student.performance.domain.ClientSystemFlag;
import tds.student.performance.domain.ClientTestProperty;

import java.util.List;

/**
 * Created by jjohnson on 12/25/15.
 */
public interface ConfigurationDao {
    List<ClientSystemFlag> getSystemFlags(String clientName);
    ClientTestProperty getClientTestProperty(String clientName, String testId);
}
