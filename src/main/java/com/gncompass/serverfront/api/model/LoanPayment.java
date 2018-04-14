package com.gncompass.serverfront.api.model;

import java.util.logging.Logger;

import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;
import javax.servlet.http.HttpServletRequest;

public class LoanPayment extends TransactionDetail {
  private static final String KEY_DUE_DATE = "due_date";
  private static final String KEY_INTEREST = "interest";
  private static final Logger LOG = Logger.getLogger(LoanPayment.class.getName());

  public long mDueDateTime = 0L;
  public double mInterest = 0.0d;

  public LoanPayment() {
  }

  public LoanPayment(double interest, long dueDateTime) {
    mInterest = interest;
    mDueDateTime = dueDateTime;
  }

  @Override
  protected void addToJson(JsonObjectBuilder jsonBuilder) {
    super.addToJson(jsonBuilder);

    jsonBuilder.add(KEY_INTEREST, mInterest);
    jsonBuilder.add(KEY_DUE_DATE, mDueDateTime);
  }

  @Override
  protected Logger getLogger() {
    return LOG;
  }

  @Override
  public boolean isValid() {
    return (super.isValid() && mInterest > 0.0d && mDueDateTime > 0L);
  }

  @Override
  public void parse(HttpServletRequest request) {
    super.parse(request);

    JsonObject jsonObject = getContent(request);
    if(jsonObject != null) {
      mInterest = getDoubleFromJson(jsonObject, KEY_INTEREST, 0.0d);
      mDueDateTime = getLongFromJson(jsonObject, KEY_DUE_DATE, 0L);
    }
  }
}
