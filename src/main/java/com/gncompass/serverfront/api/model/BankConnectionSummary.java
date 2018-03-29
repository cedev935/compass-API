package com.gncompass.serverfront.api.model;

import com.gncompass.serverfront.util.StringHelper;

import java.util.logging.Logger;

import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;
import javax.servlet.http.HttpServletRequest;

public class BankConnectionSummary extends AbstractModel {
  private static final String KEY_INSTITUTION = "institution";
  private static final String KEY_REFERENCE = "reference";
  private static final Logger LOG = Logger.getLogger(BankConnectionSummary.class.getName());

  public int mInstitution = 0;
  public String mReference = null;

  public BankConnectionSummary() {
  }

  public BankConnectionSummary(String reference, int institution) {
    mReference = reference;
    mInstitution = institution;
  }

  @Override
  protected void addToJson(JsonObjectBuilder jsonBuilder) {
    jsonBuilder.add(KEY_REFERENCE, mReference);
    jsonBuilder.add(KEY_INSTITUTION, mInstitution);
  }

  @Override
  protected Logger getLogger() {
    return LOG;
  }

  @Override
  public boolean isValid() {
    return (mInstitution > 0 &&
            mReference != null && StringHelper.isUuid(mReference));
  }

  @Override
  public void parse(HttpServletRequest request) {
    JsonObject jsonObject = getContent(request);
    if(jsonObject != null) {
      mInstitution = jsonObject.getInt(KEY_INSTITUTION, 0);
      mReference = jsonObject.getString(KEY_REFERENCE, null);
    }
  }
}
