/*******************************************************************************
 * Educational Online Test Delivery System 
 * Copyright (c) 2014 American Institutes for Research
 *       
 * Distributed under the AIR Open Source License, Version 1.0 
 * See accompanying file AIR-License-1_0.txt or at
 * http://www.smarterapp.org/documents/American_Institutes_for_Research_Open_Source_Software_License.pdf
 ******************************************************************************/
package tds.student.web.handlers;

import java.io.IOException;
import java.net.URISyntaxException;

import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import tds.itempreview.Config;
import tds.itempreview.ConfigLoader;
import tds.itempreview.ItemPreviewSettings;

@Controller
@Scope ("prototype")
public class ItemPreviewHandler extends TDSHandler
{
  // Controller starts here
  @RequestMapping (value = "API.axd/config", produces = "application/json")
  @ResponseBody
  public Config loadConfig () throws IOException, URISyntaxException {
    ConfigLoader configLoader = new ConfigLoader ();
    Config config = configLoader.load ();

    int cacheMins = ItemPreviewSettings.getConfigCacheMinutes ().getValue ();
    if (cacheMins > 0) {
      // TODO Shiva: Set cache timeout.
    }
    return config;
  }

  @Override
  protected void onBeanFactoryInitialized () {

  }
}
