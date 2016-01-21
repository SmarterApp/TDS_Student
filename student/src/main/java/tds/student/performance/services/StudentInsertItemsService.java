package tds.student.performance.services;

import AIR.Common.DB.SQLConnection;
import AIR.Common.DB.results.MultiDataResultSet;
import TDS.Shared.Exceptions.ReturnStatusException;

import java.util.UUID;

public interface StudentInsertItemsService {

    MultiDataResultSet insertItems(SQLConnection connection, UUID oppKey, UUID sessionKey, UUID browserId,
                                        Integer segment, String segmentId, Integer page, String groupId,
                                        String itemKeys, Character delimiter,
                                        Integer groupItemsRequired, Float groupB) throws ReturnStatusException;

}
