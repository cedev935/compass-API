package com.gncompass.serverfront.api.executer;

import com.gncompass.serverfront.db.model.LoanFrequency;
import com.gncompass.serverfront.util.HttpHelper;

import java.io.IOException;
import java.util.List;

import javax.json.Json;
import javax.json.JsonArrayBuilder;
import javax.json.JsonObjectBuilder;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.ServletException;

public class LoanFrequenciesGet extends AbstractExecuter {

  public LoanFrequenciesGet() {
  }

  @Override
  protected void execute(HttpServletResponse response) throws ServletException, IOException {
    List<LoanFrequency> loanFrequencies = LoanFrequency.getAll();
    JsonArrayBuilder arrayBuilder = Json.createArrayBuilder();
    for (LoanFrequency lf : loanFrequencies) {
      JsonObjectBuilder objectBuilder = lf.getApiModel().toJsonBuilder();
      if (objectBuilder != null) {
        arrayBuilder.add(objectBuilder);
      }
    }
    HttpHelper.setResponseSuccess(response, arrayBuilder.build());
  }

  @Override
  protected int getInvalidErrorCode() {
    return 0;
  }

  @Override
  protected String getInvalidErrorString() {
    return "Not implemented";
  }

  @Override
  protected boolean validate(HttpServletRequest request) {
    return true;
  }
}
