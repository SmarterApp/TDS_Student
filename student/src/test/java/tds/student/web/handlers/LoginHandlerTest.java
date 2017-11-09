/***************************************************************************************************
 * Copyright 2017 Regents of the University of California. Licensed under the Educational
 * Community License, Version 2.0 (the “license”); you may not use this file except in
 * compliance with the License. You may obtain a copy of the license at
 *
 * https://opensource.org/licenses/ECL-2.0
 *
 * Unless required under applicable law or agreed to in writing, software distributed under the
 * License is distributed in an “AS IS” BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for specific language governing permissions
 * and limitations under the license.
 **************************************************************************************************/

package tds.student.web.handlers;

import AIR.test.framework.AbstractTest;
import TDS.Shared.Exceptions.ReturnStatusException;
import org.junit.Test;

import java.sql.SQLException;

import tds.student.services.data.LoginInfo;

import static org.junit.Assert.assertTrue;

public class LoginHandlerTest extends AbstractTest
{
  MasterShellHandler handler = new MasterShellHandler();

  @Test
  public void secureBrowserRequiredAndIsBeingUsed() throws SQLException, ReturnStatusException {
    LoginInfo loginInfo = new LoginInfo();
    handler.checkSecureBrowserRequired(true, true, loginInfo);

    assertTrue(loginInfo.getValidationErrors().size() == 0);
  }

  @Test
  public void secureBrowserRequiredAndIsNotBeingUsed() throws SQLException, ReturnStatusException {
    LoginInfo loginInfo = new LoginInfo();
    handler.checkSecureBrowserRequired(false, true, loginInfo);

    assertTrue(loginInfo.getValidationErrors().size() == 1);
  }

  @Test
  public void secureBrowserNotRequiredAndIsNotBeingUsed() throws SQLException, ReturnStatusException {
    LoginInfo loginInfo = new LoginInfo();
    handler.checkSecureBrowserRequired(false, false, loginInfo);

    assertTrue(loginInfo.getValidationErrors().size() == 0);
  }

  @Test
  public void secureBrowserNotRequiredAndIsBeingUsed() throws SQLException, ReturnStatusException {
    LoginInfo loginInfo = new LoginInfo();
    handler.checkSecureBrowserRequired(true, false, loginInfo);

    assertTrue(loginInfo.getValidationErrors().size() == 0);
  }
}
