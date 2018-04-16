package com.gncompass.serverfront.api.model;

import com.gncompass.serverfront.util.StringHelper;

import java.util.logging.Logger;

import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;
import javax.servlet.http.HttpServletRequest;

public class BankConnectionSummary extends AbstractModel {
  private static final String KEY_INSTITUTION = "institution";
  private static final String KEY_NAME = "name";
  private static final String KEY_REFERENCE = "reference";
  private static final Logger LOG = Logger.getLogger(BankConnectionSummary.class.getName());

  public int mInstitution = 0;
  public String mName = null;
  public String mReference = null;

  public BankConnectionSummary() {
  }

  public BankConnectionSummary(JsonObject jsonObject) {
    parse(jsonObject);
  }

  public BankConnectionSummary(String reference, int institution, String name) {
    mReference = reference;
    mInstitution = institution;
    mName = name;
  }

  @Override
  protected void addToJson(JsonObjectBuilder jsonBuilder) {
    jsonBuilder.add(KEY_REFERENCE, mReference);
    jsonBuilder.add(KEY_INSTITUTION, mInstitution);
    jsonBuilder.add(KEY_NAME, mName);
  }

  @Override
  protected Logger getLogger() {
    return LOG;
  }

  @Override
  public boolean isValid() {
    return (mInstitution > 0 &&
            mName != null && mName.length() > 0 &&
            mReference != null && StringHelper.isUuid(mReference));
  }

  @Override
  public void parse(HttpServletRequest request) {
    parse(getContent(request));
  }

  private void parse(JsonObject jsonObject) {
    if(jsonObject != null) {
      mInstitution = jsonObject.getInt(KEY_INSTITUTION, 0);
      mName = jsonObject.getString(KEY_NAME, null);
      mReference = jsonObject.getString(KEY_REFERENCE, null);
    }
  }
}
