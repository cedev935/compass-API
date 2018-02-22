package com.gncompass.serverfront.api.parser;

import com.gncompass.serverfront.api.executer.AbstractExecuter;
import com.gncompass.serverfront.api.executer.BorrowerCreate;
import com.gncompass.serverfront.util.HttpHelper.RequestType;

import java.io.IOException;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.ServletException;

public abstract class BorrowerParser {
  private static final String PATH_LOGIN = "login";
  private static final String PATH_LOGOUT = "logout";
  public static final String PATH_MAIN = "borrowers";

  public static void parseRequest(List<String> pathChunks, RequestType type,
                                  HttpServletRequest request, HttpServletResponse response)
      throws ServletException, IOException {
    AbstractExecuter executer = null;
    boolean nextLevel = false;

    // Level 1: /borrowers
    if (pathChunks.size() == 0) {
      if (type == RequestType.POST) {
        executer = new BorrowerCreate();
      }
    } else {
      nextLevel = true;
    }

    // Level 2: /borrowers/{chunk}
    String level2Chunk = null;
    if (nextLevel) {
      nextLevel = false;
      level2Chunk = pathChunks.remove(0);

      switch (level2Chunk) {
        case PATH_LOGIN:
          if (pathChunks.size() == 0 && type == RequestType.POST) {
            // TODO! Implement
          }
          break;
        default:
          if (pathChunks.size() == 0) {
            if (type == RequestType.GET) {
              // TODO! Implement
            } else if (type == RequestType.PUT) {
              // TODO! Implement
            }
          } else {
            nextLevel = true;
          }
          break;
      }
    }

    // Level 3: /borrowers/{UUID}/{chunk}
    if (nextLevel) {
      nextLevel = false;

      switch (pathChunks.remove(0)) {
        case PATH_LOGOUT:
          if (pathChunks.size() == 0 && type == RequestType.POST) {
            // TODO! Implement
          }
          break;
      }
    }

    // Process the execution
    if (executer != null) {
      executer.process(request, response);
    } else {
      response.setStatus(HttpServletResponse.SC_NOT_FOUND);
    }
  }
}
