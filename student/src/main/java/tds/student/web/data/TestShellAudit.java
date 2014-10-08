/*******************************************************************************
 * Educational Online Test Delivery System 
 * Copyright (c) 2014 American Institutes for Research
 *     
 * Distributed under the AIR Open Source License, Version 1.0 
 * See accompanying file AIR-License-1_0.txt or at
 * http://www.smarterapp.org/documents/American_Institutes_for_Research_Open_Source_Software_License.pdf
 ******************************************************************************/
package tds.student.web.data;

import java.util.List;

import tds.student.sql.data.ClientLatency;
import tds.student.sql.data.ToolUsed;

/**
 * @author mpatel
 *
 */
public class TestShellAudit
{
      private List<ClientLatency> latencies;

      private List<ToolUsed> toolsUsed ;

      public List<ClientLatency> getLatencies () {
        return latencies;
      }

      public void setLatencies (List<ClientLatency> latencies) {
        this.latencies = latencies;
      }

      public List<ToolUsed> getToolsUsed () {
        return toolsUsed;
      }

      public void setToolsUsed (List<ToolUsed> toolsUsed) {
        this.toolsUsed = toolsUsed;
      }
      
      
      
}
