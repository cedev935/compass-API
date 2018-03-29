package com.gncompass.serverfront.api.model;

import com.gncompass.serverfront.util.StringHelper;

import java.util.logging.Logger;

import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;
import javax.servlet.http.HttpServletRequest;

public class BankConnectionNew extends AbstractModel {
  private static final String KEY_ACCOUNT = "account";
  private static final String KEY_BANK_ID = "bank_id";
  private static final String KEY_LOGIN_ID = "login_id";
  private static final String KEY_TRANSIT = "transit";
  private static final Logger LOG = Logger.getLogger(BankConnectionNew.class.getName());

  public int mAccount = 0;
  public long mBankId = 0;
  public String mLoginId = null;
  public int mTransit = 0;

  public BankConnectionNew() {
  }

  public BankConnectionNew(String loginId, long bankId, int transit, int account) {
    mAccount = account;
    mBankId = bankId;
    mLoginId = loginId;
    mTransit = transit;
  }

  @Override
  protected void addToJson(JsonObjectBuilder jsonBuilder) {
    jsonBuilder.add(KEY_LOGIN_ID, mLoginId);
    jsonBuilder.add(KEY_BANK_ID, mBankId);
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
            mBankId > 0L && mTransit > 0 && mAccount > 0);
  }

  @Override
  public void parse(HttpServletRequest request) {
    JsonObject jsonObject = getContent(request);
    if(jsonObject != null) {
      mLoginId = jsonObject.getString(KEY_LOGIN_ID, null);
      mBankId = getLongFromJson(jsonObject, KEY_BANK_ID, 0L);
      mTransit = jsonObject.getInt(KEY_TRANSIT, 0);
      mAccount = jsonObject.getInt(KEY_ACCOUNT, 0);
    }
  }
}
