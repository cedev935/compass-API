package com.gncompass.serverfront.api.executer;

import com.gncompass.serverfront.util.HttpHelper;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.ServletException;

public abstract class AbstractExecuter {
  protected abstract void execute(HttpServletResponse response) throws ServletException, IOException;
  protected abstract int getInvalidErrorCode();
  protected abstract String getInvalidErrorString();
  protected abstract boolean validate(HttpServletRequest request);

  public void process(HttpServletRequest request, HttpServletResponse response)
      throws ServletException, IOException {
    if (validate(request)) {
      execute(response);
    } else {
      HttpHelper.setResponseError(response, HttpServletResponse.SC_BAD_REQUEST,
                                  getInvalidErrorCode(), getInvalidErrorString());
    }
  }
}
