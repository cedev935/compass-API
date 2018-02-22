package com.gncompass.serverfront.api.parser;

import com.gncompass.serverfront.api.executer.AbstractExecuter;
import com.gncompass.serverfront.api.executer.CountriesGet;
import com.gncompass.serverfront.util.HttpHelper.RequestType;

import java.io.IOException;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.ServletException;

public abstract class GeneralParser {
  private static final String FUNCTION_COUNTRIES = "countries";

  public static void parseRequest(String function, List<String> pathChunks, RequestType type,
                                  HttpServletRequest request, HttpServletResponse response)
      throws ServletException, IOException {
    AbstractExecuter executer = null;

    // Level 1: /{function}
    if (pathChunks.size() == 0) {
      switch (function) {
        case FUNCTION_COUNTRIES:
          if (type == RequestType.GET) {
            executer = new CountriesGet();
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
