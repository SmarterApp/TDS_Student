package tds.student.securebrowser.controllers;

import TDS.Shared.Exceptions.ReturnStatusException;
import TDS.Shared.Messages.IMessageService;
import org.apache.http.HttpHeaders;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import tds.student.securebrowser.enums.LanguageCode;
import tds.student.securebrowser.enums.LoginKeys;
import tds.student.securebrowser.enums.TDSCookieNames;
import tds.student.services.abstractions.*;
import tds.student.services.data.LoginInfo;
import tds.student.services.data.LoginKeyValues;
import tds.student.web.StudentContext;
import tds.student.web.StudentCookie;
import tds.student.web.handlers.ContentHandler;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Created by emunoz on 2/2/16.
 */
@Controller
@Scope("prototype")
public class BrowserSessionLaunchController  {
    private static final Logger LOG = LoggerFactory.getLogger(BrowserSessionLaunchController.class);

    private static final String STUDENT_LOGIN_ERR = "Unknown session ID. Please check the Session ID value and try again.";

    private static final String STUDENT_LOGIN_ERR_MSGKEY = "SecureBrowserLaunch.UnknownSessionId";

    @Autowired
    private ILoginService _loginService;

    @Autowired
    private IMessageService _messageService;

    /**
     * This method provides an API to validate student login information (studentId and sessionId) for the Secure Browser
     * Launch Protocol. A successful login should results in a HTTP 303 response with a "Pragma" request header that
     * includes the validated sessionId and a "Location" URL for redirection. A failed login should result in a HTTP 400
     * (Bad Request) response and should include a localized error message in its response body.
     *
     * This API sets a cookie on the browser (named TDS-SB-LaunchProtocol) that will indicate to the LoginShell that the login
     * was initiated from a secure browser launch page. This API also sets the necessary cookies containing student credential
     * and other metadata (TDS-StudentData) which the LoginShell page will use to log in.
     *
     * As specified by the Secure Browser Launch Protocol specification, the login service should only validate the
     * session ID and student ID. This is indicated to the {@link tds.student.services.LoginService} via the
     * 'SBLaunchProtocol' login key. Validation of optional login field names such as FirstName, LastName, etc is not
     * currently a requirement for the secure browser session launch protocol specification 0.5 (2015-06-11).
     *
     * @param request The {@link HttpServletRequest} request object.
     * @param response The {@link HttpServletResponse} response object.
     * @param sessionId The session ID of the session the student is attempting to join.
     * @param studentId The student ID of the student who is attempting to log in.
     *
     * @return A null {@link String} if successful. Otherwise, a localized error string should be returned.
     */
    @RequestMapping(value = "browsersessionlaunch", method = RequestMethod.GET, produces = MediaType.TEXT_PLAIN_VALUE)
    @ResponseBody
    public String browserSessionLaunch(HttpServletRequest request, HttpServletResponse response,
                                       @RequestParam(value = "sessionid") String sessionId,
                                       @RequestParam(value = "studentid") String studentId) {
        LoginInfo loginInfo;
        boolean loginFailed = false;
        String responseText = null;
        LoginKeyValues loginKeyValues = new LoginKeyValues();
        loginKeyValues.put(LoginKeys.STUDENTID.getKeyName(), studentId);
        //Set the secure browser launch protocol login key to tell the login service to validate only student and session ID.
        loginKeyValues.put(LoginKeys.SECURE_BROWSER_LAUNCH_PROTOCOL.getKeyName(), Boolean.TRUE.toString());

        try {
            loginInfo = _loginService.login(sessionId.toUpperCase(), loginKeyValues);

            if (loginInfo != null && loginInfo.getValidationErrors().isEmpty()) {
                String pragma = "sessionid=\"" + sessionId.toUpperCase() + "\"";
                // Get path of student app and set as redirection url
                String requestUrl = request.getRequestURI().replace("/browsersessionlaunch", "");
                String locationUrl = String.format("https://%s%s", request.getHeader("Host"), requestUrl);
                response.setHeader(HttpHeaders.PRAGMA, pragma);
                response.setHeader(HttpHeaders.LOCATION, locationUrl);
                response.setStatus(HttpServletResponse.SC_SEE_OTHER); //303 and 302 indicate a successful login
                setResponseCookies(response, loginInfo);
            } else {
                loginFailed = true;
            }
        } catch (Exception ex) {
            LOG.error("Error logging in via browser session launch protocol: " + ex.getMessage(), ex);
            loginFailed = true;
        }

        if (loginFailed) {
            LOG.error("Login failed for student ID {} and sessionID {}.", studentId, sessionId);
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            responseText = getTranslatedErrorText(request.getHeader(HttpHeaders.ACCEPT_LANGUAGE));

        }
        return responseText;
    }

    /**
     * Translate the error text based on ISO 639 language code provided in the Accept-Language header.
     *
     * @param isoLangCode
     * @return
     */
    private String getTranslatedErrorText(final String isoLangCode) {
        String responseText;
        // Accept-Language header contains ISO 639 codes (en/es), still need to translate to MFC code
        try {
            // Make sure to get the message using the MFC language code (ESN/ENU instead of es/en)
            responseText = _messageService.get("browsersessionlaunch", LanguageCode.getMfcCodeFromIsoCode(isoLangCode),
                    STUDENT_LOGIN_ERR_MSGKEY);
        }
        catch (ReturnStatusException e) {
            //Default to English generic error message
            LOG.warn("Secure Browser Launch Protocol: Could not retrieve localize error message.");
            responseText = STUDENT_LOGIN_ERR;
        }
        return responseText;
    }

    /**
     * Set the TDS-SB-LaunchProtocol, TDS-Student-Data and other related login session cookies.
     *
     * @param response
     * @param loginInfo
     */
    private void setResponseCookies(final HttpServletResponse response, final LoginInfo loginInfo) {
        // Save student info cookie
        StudentContext.saveTestee(loginInfo.getTestee());
        StudentContext.saveSession(loginInfo.getSession());
        // Save secure browser cookie so that loginshell knows that this is a redirect (and to not clear student data)
        Cookie tdsSbCookie = new Cookie(TDSCookieNames.TDS_SECURE_BROWSER_LAUNCH_PROTOCOL.getCookieName(), "true");
        tdsSbCookie.setPath("/"); // Set to root path so cookie can be erased via javascript after redirected login
        response.addCookie(tdsSbCookie);
        StudentCookie.writeStore();
    }

}
