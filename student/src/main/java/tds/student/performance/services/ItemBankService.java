package tds.student.performance.services;

import TDS.Shared.Exceptions.ReturnStatusException;
import tds.student.sql.data.AccList;
import tds.student.sql.data.TestForm;
import tds.student.sql.data.TestProperties;

import java.util.List;

public interface ItemBankService {
    List<String> listTests() throws ReturnStatusException;

    TestProperties getTestProperties(String testKey) throws ReturnStatusException;

    AccList getTestAccommodations(String testKey) throws ReturnStatusException;

    List<String> getGrades() throws ReturnStatusException;

    List<TestForm> getTestForms(String testID) throws ReturnStatusException;

    String getItemPath(long bankKey, long itemKey) throws ReturnStatusException;

    String getStimulusPath(long bankKey, long stimulusKey) throws ReturnStatusException;
}
