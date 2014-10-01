/*******************************************************************************
 * Educational Online Test Delivery System 
 * Copyright (c) 2014 American Institutes for Research
 *     
 * Distributed under the AIR Open Source License, Version 1.0 
 * See accompanying file AIR-License-1_0.txt or at
 * http://www.smarterapp.org/documents/American_Institutes_for_Research_Open_Source_Software_License.pdf
 ******************************************************************************/
package tds.student.sql.repository;

/**
 * @author efurman
 * 
 */

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;

import AIR.Common.DB.AbstractDAO;
import AIR.Common.DB.SQLConnection;
import AIR.Common.DB.results.DbResultRecord;
import AIR.Common.DB.results.SingleDataResultSet;
import AIR.Common.Helpers._Ref;
import TDS.Shared.Messages.IMessageRepository;
import TDS.Shared.Exceptions.ReturnStatusException;

import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import tds.dll.api.IProctorDLL;
import TDS.Shared.Messages.ContextType;
import TDS.Shared.Messages.MessageDTO;

@Component
@Scope ("prototype")
public class MessageRepository extends AbstractDAO implements IMessageRepository
{  
  private static final Logger _logger = LoggerFactory.getLogger(MessageRepository.class);
  @Autowired
  IProctorDLL    dll     = null;

  public MessageRepository () {
	   super();
  }

  /**
  * Gets all the message translations available for a list of contexts.
  * This will return ServerSide and ClientSide messages.
  */
  public List<MessageDTO> getMessages (String language, String contextlist) throws ReturnStatusException {
    // check for language
    if (StringUtils.isEmpty (language)) {
      _logger.error ("If the language is NULL then the wrong client is loaded.");
      throw new ReturnStatusException (new Exception ("If the language is NULL then the wrong client is loaded."));
    }
    List<MessageDTO> messages = new ArrayList<MessageDTO> ();

    Character delimiter = '|';
    try (SQLConnection connection = getSQLConnection ()) {

      SingleDataResultSet result = dll.AppMessagesByContext_SP (connection, getTdsSettings().getAppName(), getTdsSettings().getClientName(), language, contextlist, delimiter);
      //ReturnStatusException.getInstanceIfAvailable (result);

      Iterator<DbResultRecord> records = result.getRecords ();
      result.setFixNulls (true);
      result.setFixMissing (true);

      while (records.hasNext ()) {
        DbResultRecord record = records.next ();
        
        MessageDTO messageDTO = new MessageDTO ();
        messageDTO.setMessageSource(record.<String> get("msgSource"));
        messageDTO.setContextType(ContextType.getContextTypeFromStringCaseInsensitive(record.<String> get("ContextType")));
        messageDTO.setContext(record.<String> get("Context"));
        messageDTO.setMessageId(record.<Integer> get("MessageID"));
        messageDTO.setAppKey(record.<String> get("AppKey"));
        messageDTO.setLanguage(record.<String> get("Language"));
        messageDTO.setGrade(record.<String> get("Grade"));
        messageDTO.setSubject(record.<String> get("Subject"));
        messageDTO.setMessage(record.<String> get("Message"));
        
        messages.add(messageDTO);
      }
      
      if (messages.size () == 0) {
        // if (reader == null) {
        _logger.error ("The SP AppMessagesByContext did not return any records");
        throw new ReturnStatusException (new Exception ("The SP AppMessagesByContext did not return any records"));
      }

    } catch (SQLException e) {
      _logger.error (e.getMessage ());
      throw new ReturnStatusException  (e);
    }
    return messages;
  }

  /** Gets a single message translation.
  *
  * Returns the translated message and the message ID in brackets.
  * If the translation is not found then will return the app key that was
  * passed in.
  */
  public String getMessage (String contextType, String context, String appKey, String language) throws ReturnStatusException {

    _Ref<String> message = new _Ref<String> ();
    try (SQLConnection connection = getSQLConnection ()) {
      dll.All_FormatMessage_SP (connection, getTdsSettings().getClientName(), language, getTdsSettings().getAppName(), contextType, context, appKey, message);

      if (StringUtils.isEmpty (message.get()))
        return appKey;
        
      return message.get();
      
    } catch (SQLException e) {
      _logger.error (e.getMessage ());
      throw new ReturnStatusException (e);
    }
  }
}
