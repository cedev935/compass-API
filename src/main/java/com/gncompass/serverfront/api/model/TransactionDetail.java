package com.gncompass.serverfront.api.model;

import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;
import javax.servlet.http.HttpServletRequest;

public abstract class TransactionDetail extends AbstractModel {
  private static final String KEY_AMOUNT = "amount";
  private static final String KEY_PAID_DATE = "paid_date";

  public double mAmount = 0.0d;
  public long mPaidDateTime = 0L;

  @Override
  protected void addToJson(JsonObjectBuilder jsonBuilder) {
    jsonBuilder.add(KEY_AMOUNT, mAmount);
    if (mPaidDateTime > 0L) {
      jsonBuilder.add(KEY_PAID_DATE, mPaidDateTime);
    }
  }

  @Override
  public boolean isValid() {
    return mAmount > 0.0d;
  }

  @Override
  public void parse(HttpServletRequest request) {
    JsonObject jsonObject = getContent(request);
    if(jsonObject != null) {
      mAmount = getDoubleFromJson(jsonObject, KEY_AMOUNT, 0.0d);
      mPaidDateTime = getLongFromJson(jsonObject, KEY_PAID_DATE, 0L);
    }
  }

  public void setTransactionData(double amount, long paidDateTime) {
    mAmount = amount;
    mPaidDateTime = paidDateTime;
  }
}
