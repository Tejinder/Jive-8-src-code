/*
 * Copyright (C) 1999-2015 Jive Software. All rights reserved.
 *
 * This software is the proprietary information of Jive Software. Use is subject to license terms.
 */

package com.jivesoftware.community.aaa;

import com.grail.util.BATConstants;
import com.jivesoftware.community.Captcha;
import com.jivesoftware.community.JiveGlobals;
import com.jivesoftware.community.action.DisclaimerAction;
import com.jivesoftware.util.StringUtils;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

public class JiveLoginThrottleProcessingFilter extends JiveAuthenticationProcessingFilter {

    private LoginThrottle loginThrottle;
    private Captcha captcha;
    private LoginThrottledRedirectStrategy loginThrottledRedirectHandler;
    private InvalidCaptchaGuessRedirectStrategy invalidCaptchaGuessRedirectStrategy;
    private static final String LOGIN_THROTTLE_ENABLED = "login.throttle.enabled";

    public void setLoginThrottle(LoginThrottle loginThrottle) {
        this.loginThrottle = loginThrottle;
    }

    public void setCaptcha(Captcha captcha) {
        this.captcha = captcha;
    }

    public void setLoginThrottledRedirectHandler(LoginThrottledRedirectStrategy loginThrottledRedirectHandler) {
        this.loginThrottledRedirectHandler = loginThrottledRedirectHandler;
    }

    public void setInvalidCaptchaGuessRedirectStrategy(InvalidCaptchaGuessRedirectStrategy invalidCaptchaGuessRedirectStrategy) {
        this.invalidCaptchaGuessRedirectStrategy = invalidCaptchaGuessRedirectStrategy;
    }

    public boolean isLoginThrottlingEnabled() {
        return JiveGlobals.getJiveBooleanProperty(LOGIN_THROTTLE_ENABLED, false);
    }

    @Override
    public Authentication attemptAuthentication(HttpServletRequest request, HttpServletResponse response) throws AuthenticationException {
    	DisclaimerAction.printOut(" in attemptAuthentication..roland jiveloginthrottle.");
    	if (isLoginThrottlingEnabled()) {

            
        	String captchaInput = request.getParameter(Captcha.INPUT_KEY);

            //only test for throttling if captcha input isnt specified
            if (StringUtils.isBlank(captchaInput)) {
                //if login is throttled and we arent looking for captcha, we need to show them one
                if (!loginThrottle.isLoginAllowed(obtainUsername(request))) {
                    throw new UserLoginThrottledException("Maximum number of login attempts reached.");
                }
            }

            //check valid captcha
            if (captcha.isLoginCaptchaEnabled() && !captcha.validate(obtainUsername(request), captchaInput)) {
                throw new InvalidCaptchaGuessAuthenticationException("Captcha image input is not valid");
            }
        }

        return super.attemptAuthentication(request, response);
    }

    @Override
    protected void successfulAuthentication(HttpServletRequest request, HttpServletResponse response,
            Authentication auth) throws IOException, ServletException
    {
        if (isLoginThrottlingEnabled()) {
            loginThrottle.recordSuccess(obtainUsername(request));
        }
        request.getSession().setAttribute(BATConstants.GRAIL_FORCE_PORTAL_OPTIONS, true);
        super.successfulAuthentication(request, response, auth);    //call super so we store auth in context
    }

    @Override
    protected void unsuccessfulAuthentication(HttpServletRequest request, HttpServletResponse response,
            AuthenticationException failed) throws IOException, ServletException
    {
        if (isLoginThrottlingEnabled()) {
            String username = obtainUsername(request);
            loginThrottle.recordFailure(username);

            //login has been throttled, redirect and short circuit
            if (UserLoginThrottledException.class.equals(failed.getClass())) {
                loginThrottledRedirectHandler.sendRedirect(request, response, "");
                return;
            }

            if (InvalidCaptchaGuessAuthenticationException.class.equals(failed.getClass())) {
                invalidCaptchaGuessRedirectStrategy.sendRedirect(request, response, "");
                return;
            }
        }

        super.unsuccessfulAuthentication(request, response, failed);  //call super to clear auth context
    }
}
