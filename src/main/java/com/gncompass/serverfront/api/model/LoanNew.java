package com.gncompass.serverfront.api.model;

import com.gncompass.serverfront.util.StringHelper;

import java.util.logging.Logger;

import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;
import javax.servlet.http.HttpServletRequest;

public class LoanNew extends AbstractModel {
  private static final String KEY_AMORTIZATION = "amortization";
  private static final String KEY_BANK = "bank";
  private static final String KEY_FREQUENCY = "frequency";
  private static final String KEY_PRINCIPAL = "principal";
  private static final Logger LOG = Logger.getLogger(LoanNew.class.getName());
  private static final double MIN_LOAN = 100.0d;

  public int mAmortizationId = 0;
  public String mBankUuid = null;
  public int mFrequencyId = 0;
  public double mPrincipal = 0.0d;

  public LoanNew() {
  }

  @Override
  protected void addToJson(JsonObjectBuilder jsonBuilder) {
    jsonBuilder.add(KEY_BANK, mBankUuid);
    jsonBuilder.add(KEY_PRINCIPAL, mPrincipal);
    jsonBuilder.add(KEY_AMORTIZATION, mAmortizationId);
    jsonBuilder.add(KEY_FREQUENCY, mFrequencyId);
  }

  @Override
  protected Logger getLogger() {
    return LOG;
  }

  @Override
  public boolean isValid() {
    return (mBankUuid != null && StringHelper.isUuid(mBankUuid) && mPrincipal >= MIN_LOAN
            && mAmortizationId > 0 && mFrequencyId > 0);
  }

  @Override
  public void parse(HttpServletRequest request) {
    JsonObject jsonObject = getContent(request);
    if(jsonObject != null) {
      mBankUuid = jsonObject.getString(KEY_BANK, null);
      mPrincipal = getDoubleFromJson(jsonObject, KEY_PRINCIPAL, 0.0d);
      mAmortizationId = jsonObject.getInt(KEY_AMORTIZATION, 0);
      mFrequencyId = jsonObject.getInt(KEY_FREQUENCY, 0);
    }
  }
}
