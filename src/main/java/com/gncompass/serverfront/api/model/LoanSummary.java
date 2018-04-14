package com.gncompass.serverfront.api.model;

import com.gncompass.serverfront.util.StringHelper;

import java.util.logging.Logger;

import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;
import javax.servlet.http.HttpServletRequest;

public class LoanSummary extends AbstractModel {
  private static final String KEY_BALANCE = "balance";
  //private static final String KEY_NEXT_PAYMENT = "next_payment";
  private static final String KEY_PRINCIPAL = "principal";
  private static final String KEY_RATE = "rate";
  private static final String KEY_REFERENCE = "reference";
  private static final String KEY_STARTED = "started";
  private static final Logger LOG = Logger.getLogger(LoanSummary.class.getName());

  public Double mBalance = null;
  //public LoanPayment mNextPayment = null;
  public double mPrincipal = 0.0d;
  public double mRate = 0.0d;
  public String mReference = null;
  public long mStartedTime = 0L;

  public LoanSummary() {
  }

  public LoanSummary(String reference, double principal, double rate) {
    mReference = reference;
    mPrincipal = principal;
    mRate = rate;
  }

  @Override
  protected void addToJson(JsonObjectBuilder jsonBuilder) {
    jsonBuilder.add(KEY_REFERENCE, mReference);
    jsonBuilder.add(KEY_PRINCIPAL, mPrincipal);
    jsonBuilder.add(KEY_RATE, mRate);
    if (mStartedTime > 0L) {
      jsonBuilder.add(KEY_STARTED, mStartedTime);
      if (mBalance != null && mBalance >= 0.0d) {
        jsonBuilder.add(KEY_BALANCE, mBalance);
      }
    }
  }

  @Override
  protected Logger getLogger() {
    return LOG;
  }

  @Override
  public boolean isValid() {
    return (mReference != null && StringHelper.isUuid(mReference) &&
            mPrincipal > 0.0d && mRate > 0.0d);
  }

  @Override
  public void parse(HttpServletRequest request) {
    JsonObject jsonObject = getContent(request);
    if(jsonObject != null) {
      mReference = jsonObject.getString(KEY_REFERENCE, null);
      mPrincipal = getDoubleFromJson(jsonObject, KEY_PRINCIPAL, 0.0d);
      mRate = getDoubleFromJson(jsonObject, KEY_RATE, 0.0d);
      mStartedTime = getLongFromJson(jsonObject, KEY_STARTED, 0L);
      mBalance = getDoubleFromJson(jsonObject, KEY_BALANCE, -1.0d);
      if (mBalance < 0.0d) {
        mBalance = null;
      }
    }
  }
}
