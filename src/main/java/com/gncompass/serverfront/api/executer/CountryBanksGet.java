package com.gncompass.serverfront.api.executer;

import com.gncompass.serverfront.api.model.Country;
import com.gncompass.serverfront.db.model.Bank;
import com.gncompass.serverfront.util.HttpHelper;

import java.io.IOException;
import java.util.List;

import javax.json.Json;
import javax.json.JsonArrayBuilder;
import javax.json.JsonObjectBuilder;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.ServletException;

public class CountryBanksGet extends AbstractExecuter {
  private String mCountryCode = null;

  public CountryBanksGet(String countryCode) {
    mCountryCode = countryCode;
  }

  @Override
  protected void execute(HttpServletResponse response) throws ServletException, IOException {
    List<Bank> banks = Bank.getAllForCountry(mCountryCode);
    JsonArrayBuilder arrayBuilder = Json.createArrayBuilder();
    for (Bank b : banks) {
      JsonObjectBuilder objectBuilder = b.getApiModel().toJsonBuilder();
      if (objectBuilder != null) {
        arrayBuilder.add(objectBuilder);
      }
    }
    HttpHelper.setResponseSuccess(response, arrayBuilder.build());
  }

  @Override
  protected int getInvalidErrorCode() {
    return 10100;
  }

  @Override
  protected String getInvalidErrorString() {
    return "Invalid input for fetching all banks for a given country ISO-2 code";
  }

  @Override
  protected boolean validate(HttpServletRequest request) {
    return (mCountryCode != null && mCountryCode.length() == Country.CODE_LENGTH);
  }
}
