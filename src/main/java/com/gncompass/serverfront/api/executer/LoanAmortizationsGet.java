package com.gncompass.serverfront.api.executer;

import com.gncompass.serverfront.db.model.LoanAmortization;
import com.gncompass.serverfront.util.HttpHelper;

import java.io.IOException;
import java.util.List;

import javax.json.Json;
import javax.json.JsonArrayBuilder;
import javax.json.JsonObjectBuilder;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.ServletException;

public class LoanAmortizationsGet extends AbstractExecuter {

  public LoanAmortizationsGet() {
  }

  @Override
  protected void execute(HttpServletResponse response) throws ServletException, IOException {
    List<LoanAmortization> loanAmortizations = LoanAmortization.getAll();
    JsonArrayBuilder arrayBuilder = Json.createArrayBuilder();
    for (LoanAmortization la : loanAmortizations) {
      JsonObjectBuilder objectBuilder = la.getApiModel().toJsonBuilder();
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
