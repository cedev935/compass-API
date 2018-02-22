package com.gncompass.serverfront.api.executer;

import com.gncompass.serverfront.db.model.Country;
import com.gncompass.serverfront.util.HttpHelper;
import com.gncompass.serverfront.util.StringHelper;

import java.io.IOException;
import java.util.List;

import javax.json.Json;
import javax.json.JsonArrayBuilder;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.ServletException;

public class CountriesGet extends AbstractExecuter {

  public CountriesGet() {
  }

  @Override
  protected void execute(HttpServletResponse response) throws ServletException, IOException {
    List<Country> countries = Country.getAvailable();
    JsonArrayBuilder arrayBuilder = Json.createArrayBuilder();
    for (Country c : countries) {
      JsonObjectBuilder objectBuilder = c.getApiModel().toJsonBuilder();
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
