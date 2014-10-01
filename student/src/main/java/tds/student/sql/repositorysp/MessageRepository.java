/*******************************************************************************
 * Educational Online Test Delivery System 
 * Copyright (c) 2014 American Institutes for Research
 *     
 * Distributed under the AIR Open Source License, Version 1.0 
 * See accompanying file AIR-License-1_0.txt or at
 * http://www.smarterapp.org/documents/American_Institutes_for_Research_Open_Source_Software_License.pdf
 ******************************************************************************/
package tds.student.sql.repositorysp;

import java.sql.CallableStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;

import AIR.Common.DB.AbstractDAO;
import AIR.Common.DB.SQLConnection;
import AIR.Common.Utilities.TDSStringUtils;
import TDS.Shared.Data.ColumnResultSet;
import TDS.Shared.Exceptions.ReturnStatusException;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import TDS.Shared.Messages.ContextType;
import TDS.Shared.Messages.IMessageRepository;
import TDS.Shared.Messages.MessageDTO;

// TODO Shiva move this to a common project
@Component
@Scope ("prototype")
public class MessageRepository extends AbstractDAO implements IMessageRepository
{
  
  private static final Logger _logger = LoggerFactory.getLogger (MessageRepository.class);

  // TODO what to do with clientName and systemId? super does not have those
  // fields
 
  public MessageRepository () {
   super();
  }

  // / <summary>
  // / Gets all the message translations available for a list of contexts.
  // / </summary>
  // / <remarks>
  // / This will return ServerSide and ClientSide messages.
  // / </remarks>
  public List<MessageDTO> getMessages (String language, String contextlist) throws ReturnStatusException {
    // check for language
    if (StringUtils.isEmpty (language)) {
      _logger.error ("If the language is NULL then the wrong client is loaded.");
      throw new ReturnStatusException (new Exception ("If the language is NULL then the wrong client is loaded."));
    }
    List<MessageDTO> messages = new ArrayList<MessageDTO> ();

    final String cmd = "BEGIN; SET NOCOUNT ON; exec AppMessagesByContext '{0}', '{1}', '{2}', '{3}', '{4}'; end;";
    String sqlQuery = TDSStringUtils.format (cmd, getTdsSettings().getClientName (), getTdsSettings().getAppName (), language, contextlist, '|');

    ColumnResultSet reader = null;
    try (SQLConnection connection = getSQLConnection ()) {

      try (Statement callstatement = connection.createStatement ()) {
        if (callstatement.execute (sqlQuery))
          reader = ColumnResultSet.getColumnResultSet (callstatement.getResultSet ());
        if (reader == null) {
          _logger.error ("The SP AppMessagesByContext did not return any records");
          throw new ReturnStatusException (new Exception ("The SP AppMessagesByContext did not return any records"));
        }
        reader.setFixNulls (true);
        reader.setFixMissing (true);
        while (reader.next ()) {
          MessageDTO messageDTO = new MessageDTO ();
          messageDTO.setMessageSource (reader.getString ("msgSource"));

          String contextTypeString = reader.getString ("ContextType");
          ContextType contextTypeTmp;
          try {
            contextTypeTmp = ContextType.getContextTypeFromStringCaseInsensitive (contextTypeString);
          } catch (IllegalArgumentException iar) {
            _logger.error ("ContextType " + contextTypeString + "is not a legal value");
            throw new ReturnStatusException (new Exception ("ContextType " + contextTypeString + "is not a legal value"));
          }
          messageDTO.setContextType (contextTypeTmp);
          messageDTO.setContext (reader.getString ("Context"));
          messageDTO.setMessageId (reader.getInt ("MessageID"));
          messageDTO.setAppKey (reader.getString ("AppKey"));
          messageDTO.setLanguage (reader.getString ("Language"));
          messageDTO.setGrade (reader.getString ("Grade"));
          messageDTO.setSubject (reader.getString ("Subject"));
          messageDTO.setMessage (reader.getString ("Message"));

          messages.add (messageDTO);
        }
      }
    } catch (SQLException e) {
      _logger.error (e.getMessage ());
      throw new ReturnStatusException (e);
    }
    return messages;
  }

  // / <summary>
  // / Gets a single message translation.
  // / </summary>
  // / <returns>
  // / Returns the translated message and the message ID in brackets.
  // / If the translation is not found then will return the app key that was
  // passed in.
  // / </returns>
  public String getMessage (String contextType, String context, String appKey, String language) throws ReturnStatusException {

    String message = null;

    String CMD_GET_MESSAGE = "{call ALL_FormatMessage(?, ?, ?, ?, ?, ?, ?) }";
    try (SQLConnection connection = getSQLConnection ()) {
      try (CallableStatement callstatement = connection.prepareCall (CMD_GET_MESSAGE)) {
        callstatement.setString (1, getTdsSettings().getClientName ());
        callstatement.setString (2, language);
        callstatement.setString (3, getTdsSettings().getAppName ());
        callstatement.setString (4, contextType);
        callstatement.setString (5, context);
        callstatement.setString (6, appKey);
        callstatement.registerOutParameter (7, java.sql.Types.VARCHAR);

        // callstatement.executeQuery ();
        callstatement.execute ();
        message = callstatement.getString (7);
        // TODO is it how we check for DBNULL?
        if (callstatement.wasNull () || message == null) {
          return appKey;
        }
        return message;
      }
    } catch (SQLException e) {
      _logger.error (e.getMessage ());
      throw new ReturnStatusException (e);
    }
  }
}
