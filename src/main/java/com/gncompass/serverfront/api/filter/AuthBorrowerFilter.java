package com.gncompass.serverfront.api.filter;

import com.gncompass.serverfront.api.auth.Session;
import com.gncompass.serverfront.db.model.User.UserType;

import java.io.IOException;
import java.util.Enumeration;
import java.util.regex.Pattern;

import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletResponse;

public class AuthBorrowerFilter implements javax.servlet.Filter {
  // Internals
  private String subPathFilter = ".*";
  private Pattern pattern;

  /********************************************************************
   * OVERRIDES
   *******************************************************************/

  @Override
  public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
      throws IOException, ServletException {
    boolean proceed = true;

    HttpServletRequest httpRequest = (HttpServletRequest) request;
    HttpServletResponse httpResponse = (HttpServletResponse) response;

    // If not OPTIONS request, auth needs to be checked
    if(!httpRequest.getMethod().equals("OPTIONS")) {
      // Check for match. Otherwise, just proceed down the chain
      if(isPathProtected(httpRequest)) {
        boolean authSuccess = false;

        // Fetch the access key header
        String accessKey = httpRequest.getHeader(Session.ACCESS_KEY);
        if(accessKey != null) {
          Session session = new Session(httpRequest, UserType.BORROWER);
          if(session.validate(httpRequest)) {
            session.updateAccessed();
            authSuccess = true;
          }
        }

        // If authentication failed, end the call
        if(!authSuccess) {
          proceed = false;
          httpResponse.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        }
      }
    }

    // doFilter if proceed was enabled
    if(proceed) {
      chain.doFilter(httpRequest, httpResponse);
    }
  }

  @Override
  public void destroy() {}

  @Override
  public void init(FilterConfig filterConfig) throws ServletException {
    String subPathFilter = filterConfig.getInitParameter("subPathFilter");
    if (subPathFilter != null) {
      this.subPathFilter = subPathFilter;
    }
    pattern = Pattern.compile(this.subPathFilter);
  }

  /********************************************************************
   * PRIVATES
   *******************************************************************/

   private boolean isPathProtected(HttpServletRequest request) {
     return pattern.matcher(request.getRequestURI()).matches();
   }
}
