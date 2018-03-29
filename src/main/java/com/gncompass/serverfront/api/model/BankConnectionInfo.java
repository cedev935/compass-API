package com.gncompass.serverfront.api.model;

import com.gncompass.serverfront.util.StringHelper;

import java.util.logging.Logger;

import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;
import javax.servlet.http.HttpServletRequest;

public class BankConnectionInfo extends AbstractModel {
  private static final String KEY_ACCOUNT = "account";
  private static final String KEY_INSTITUTION = "institution";
  private static final String KEY_LOGIN_ID = "login_id";
  private static final String KEY_NAME = "name";
  private static final String KEY_TRANSIT = "transit";
  private static final Logger LOG = Logger.getLogger(BankConnectionInfo.class.getName());

  public int mAccount = 0;
  public int mInstitution = 0;
  public String mLoginId = null;
  public String mName = null;
  public int mTransit = 0;

  public BankConnectionInfo() {
  }

  public BankConnectionInfo(String loginId, int institution, String name, int transit,
                            int account) {
    mAccount = account;
    mInstitution = institution;
    mLoginId = loginId;
    mName = name;
    mTransit = transit;
  }

  @Override
  protected void addToJson(JsonObjectBuilder jsonBuilder) {
    jsonBuilder.add(KEY_LOGIN_ID, mLoginId);
    jsonBuilder.add(KEY_INSTITUTION, mInstitution);
    jsonBuilder.add(KEY_NAME, mName);
    jsonBuilder.add(KEY_TRANSIT, mTransit);
    jsonBuilder.add(KEY_ACCOUNT, mAccount);
  }

  @Override
  protected Logger getLogger() {
    return LOG;
  }

  @Override
  public boolean isValid() {
    return (mLoginId != null && StringHelper.isUuid(mLoginId) &&
            mInstitution > 0 && mName != null && mName.length() > 0 &&
            mTransit > 0 && mAccount > 0);
  }

  @Override
  public void parse(HttpServletRequest request) {
    JsonObject jsonObject = getContent(request);
    if(jsonObject != null) {
      mLoginId = jsonObject.getString(KEY_LOGIN_ID, null);
      mInstitution = jsonObject.getInt(KEY_INSTITUTION, 0);
      mName = jsonObject.getString(KEY_NAME, null);
      mTransit = jsonObject.getInt(KEY_TRANSIT, 0);
      mAccount = jsonObject.getInt(KEY_ACCOUNT, 0);
    }
  }
}
