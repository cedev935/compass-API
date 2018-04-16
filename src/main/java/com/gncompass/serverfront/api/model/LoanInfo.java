package com.gncompass.serverfront.api.model;

import com.gncompass.serverfront.util.StringHelper;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonArrayBuilder;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;
import javax.servlet.http.HttpServletRequest;

public class LoanInfo extends AbstractModel {
  private static final String KEY_AMORTIZATION = "amortization";
  private static final String KEY_BALANCE = "balance";
  private static final String KEY_BANK = "bank";
  private static final String KEY_CREATED = "created";
  private static final String KEY_FREQUENCY = "frequency";
  private static final String KEY_NEXT_PAYMENT = "next_payment";
  private static final String KEY_PAYMENTS = "payments";
  private static final String KEY_PRINCIPAL = "principal";
  private static final String KEY_RATE = "rate";
  private static final String KEY_RATING = "rating";
  private static final String KEY_REFERENCE = "reference";
  private static final String KEY_STARTED = "started";
  private static final Logger LOG = Logger.getLogger(LoanInfo.class.getName());

  public LoanAmortization mAmortization = null;
  public Double mBalance = null;
  public BankConnectionSummary mBank = null;
  public long mCreatedTime = 0L;
  public LoanFrequency mFrequency = null;
  public LoanPayment mNextPayment = null;
  public List<LoanPayment> mPayments = new ArrayList<>();
  public double mPrincipal = 0.0d;
  public double mRate = 0.0d;
  public int mRatingId = 0;
  public String mReference = null;
  public long mStartedTime = 0L;

  public LoanInfo() {
  }

  public LoanInfo(String reference, long createdTime, BankConnectionSummary bank, double principal,
                  int ratingId, double rate, LoanAmortization amortization,
                  LoanFrequency frequency) {
    mReference = reference;
    mCreatedTime = createdTime;
    mBank = bank;
    mPrincipal = principal;
    mRatingId = ratingId;
    mRate = rate;
    mAmortization = amortization;
    mFrequency = frequency;
  }

  @Override
  protected void addToJson(JsonObjectBuilder jsonBuilder) {
    jsonBuilder.add(KEY_REFERENCE, mReference);
    jsonBuilder.add(KEY_CREATED, mCreatedTime);
    jsonBuilder.add(KEY_BANK, mBank.toJsonBuilder());
    jsonBuilder.add(KEY_PRINCIPAL, mPrincipal);
    jsonBuilder.add(KEY_RATING, mRatingId);
    jsonBuilder.add(KEY_RATE, mRate);
    jsonBuilder.add(KEY_AMORTIZATION, mAmortization.toJsonBuilder());
    jsonBuilder.add(KEY_FREQUENCY, mFrequency.toJsonBuilder());
    if (mStartedTime > 0L) {
      jsonBuilder.add(KEY_STARTED, mStartedTime);
      if (mBalance != null && mBalance >= 0.0d) {
        jsonBuilder.add(KEY_BALANCE, mBalance);
      }

      if (mPayments.size() > 0) {
        JsonArrayBuilder paymentArrayBuilder = Json.createArrayBuilder();
        for (LoanPayment lp : mPayments) {
          paymentArrayBuilder.add(lp.toJsonBuilder());
        }
        jsonBuilder.add(KEY_PAYMENTS, paymentArrayBuilder);
      }

      if (mNextPayment != null) {
        jsonBuilder.add(KEY_NEXT_PAYMENT, mNextPayment.toJsonBuilder());
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
            mCreatedTime > 0L && mBank != null && mBank.isValid() &&
            mPrincipal > 0.0d && mRate > 0.0d && mRatingId > 0 &&
            mAmortization != null && mAmortization.isValid() &&
            mFrequency != null && mFrequency.isValid());
  }

  @Override
  public void parse(HttpServletRequest request) {
    JsonObject jsonObject = getContent(request);
    if(jsonObject != null) {
      mReference = jsonObject.getString(KEY_REFERENCE, null);
      mCreatedTime = getLongFromJson(jsonObject, KEY_CREATED, 0L);
      mBank = new BankConnectionSummary(jsonObject.getJsonObject(KEY_BANK));
      mPrincipal = getDoubleFromJson(jsonObject, KEY_PRINCIPAL, 0.0d);
      mRatingId = jsonObject.getInt(KEY_RATING, 0);
      mRate = getDoubleFromJson(jsonObject, KEY_RATE, 0.0d);
      mAmortization = new LoanAmortization(jsonObject.getJsonObject(KEY_AMORTIZATION));
      mFrequency = new LoanFrequency(jsonObject.getJsonObject(KEY_FREQUENCY));
      mStartedTime = getLongFromJson(jsonObject, KEY_STARTED, 0L);

      mBalance = getDoubleFromJson(jsonObject, KEY_BALANCE, -1.0d);
      if (mBalance < 0.0d) {
        mBalance = null;
      }

      mPayments.clear();
      JsonArray paymentsArray = jsonObject.getJsonArray(KEY_PAYMENTS);
      if (paymentsArray != null) {
        for (int i = 0; i < paymentsArray.size(); i++) {
          mPayments.add(new LoanPayment(paymentsArray.getJsonObject(i)));
        }
      }

      mNextPayment = new LoanPayment(jsonObject);
      if (!mNextPayment.isValid()) {
        mNextPayment = null;
      }
    }
  }
}
