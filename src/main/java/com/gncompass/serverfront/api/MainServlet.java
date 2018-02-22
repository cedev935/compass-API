package com.gncompass.serverfront.api;

import com.gncompass.serverfront.api.parser.BorrowerParser;
import com.gncompass.serverfront.db.SQLManager;
import com.gncompass.serverfront.util.HttpHelper;
import com.gncompass.serverfront.util.HttpHelper.RequestType;

import java.io.IOException;
import java.util.List;
import java.util.Properties;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;

public class MainServlet extends HttpServlet {

  @Override
  public void init(ServletConfig config) throws ServletException {
    // General init of SQL system
    SQLManager.init();
  }

  @Override
  protected void doDelete(HttpServletRequest request, HttpServletResponse response)
      throws ServletException, IOException {
    parseRequest(RequestType.DELETE, request, response);
  }

  @Override
  protected void doGet(HttpServletRequest request, HttpServletResponse response)
      throws ServletException, IOException {
    parseRequest(RequestType.GET, request, response);
  }

  @Override
  protected void doHead(HttpServletRequest request, HttpServletResponse response)
      throws ServletException, IOException {
    // Not implemented and not permitted
    response.setStatus(HttpServletResponse.SC_METHOD_NOT_ALLOWED);
  }

  @Override
  protected void doOptions(HttpServletRequest request, HttpServletResponse response)
      throws ServletException, IOException {
    // Just return OK. The headers are added via filter. For dev due to swagger limitations
    response.setStatus(HttpServletResponse.SC_OK);
  }

  @Override
  protected void doPost(HttpServletRequest request, HttpServletResponse response)
      throws ServletException, IOException {
    parseRequest(RequestType.POST, request, response);
  }

  @Override
  protected void doPut(HttpServletRequest request, HttpServletResponse response)
      throws ServletException, IOException {
    parseRequest(RequestType.PUT, request, response);
  }

  @Override
  protected void doTrace(HttpServletRequest request, HttpServletResponse response)
      throws ServletException, IOException {
    // Not implemented and not permitted
    response.setStatus(HttpServletResponse.SC_METHOD_NOT_ALLOWED);
  }

  public void parseRequest(RequestType type, HttpServletRequest request, HttpServletResponse response)
      throws ServletException, IOException {
    List<String> pathChunks = HttpHelper.parseFullUrl(request);
    if (pathChunks.size() > 0) {
      switch (pathChunks.remove(0)) {
        case BorrowerParser.PATH_MAIN:
          BorrowerParser.parseRequest(pathChunks, type, request, response);
          break;
        case "investors":
          // TODO!
          response.setStatus(HttpServletResponse.SC_NOT_IMPLEMENTED);
          break;
        default:
          // TODO!
          response.setStatus(HttpServletResponse.SC_NOT_IMPLEMENTED);
          break;
      }
    } else {
      response.setStatus(HttpServletResponse.SC_NOT_FOUND);
    }
  }
}
