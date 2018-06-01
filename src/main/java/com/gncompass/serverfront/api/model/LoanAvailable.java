package com.gncompass.serverfront.api.model;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonArrayBuilder;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;
import javax.servlet.http.HttpServletRequest;

public class LoanAvailable extends AbstractModel {
  private static final String KEY_AMORTIZATIONS = "amortizations";
  private static final String KEY_ASSESSMENT = "assessment";
  private static final String KEY_FREQUENCIES = "frequencies";
  private static final String KEY_LOAN_CAP = "loan_cap";
  private static final Logger LOG = Logger.getLogger(LoanAvailable.class.getName());

  public List<LoanAmortization> mAmortizations = null;
  public AssessmentInfo mAssessment = null;
  public List<LoanFrequency> mFrequencies = null;
  public float mLoanCap = 0.0f;

  public LoanAvailable() {
  }

  public LoanAvailable(float loanCap, AssessmentInfo assessment,
                       List<LoanAmortization> amortizations, List<LoanFrequency> frequencies) {
    mLoanCap = loanCap;
    mAssessment = assessment;
    mAmortizations = amortizations;
    mFrequencies = frequencies;
  }

  @Override
  protected void addToJson(JsonObjectBuilder jsonBuilder) {
    // Core info
    jsonBuilder.add(KEY_LOAN_CAP, mLoanCap);
    jsonBuilder.add(KEY_ASSESSMENT, mAssessment.toJsonBuilder());

    // Amortizations
    JsonArrayBuilder amortizationsArray = Json.createArrayBuilder();
    for (LoanAmortization la : mAmortizations) {
      JsonObjectBuilder amortization = la.toJsonBuilder();
      if (amortization != null) {
        amortizationsArray.add(amortization);
      }
    }
    jsonBuilder.add(KEY_AMORTIZATIONS, amortizationsArray);

    // Frequencies
    JsonArrayBuilder frequenciesArray = Json.createArrayBuilder();
    for (LoanFrequency lf : mFrequencies) {
      JsonObjectBuilder frequency = lf.toJsonBuilder();
      if (frequency != null) {
        frequenciesArray.add(frequency);
      }
    }
    jsonBuilder.add(KEY_FREQUENCIES, frequenciesArray);
  }

  @Override
  protected Logger getLogger() {
    return LOG;
  }

  @Override
  public boolean isValid() {
    return (mLoanCap >= 0.0f && mAssessment != null && mAssessment.isValid()
            && mAmortizations != null && mAmortizations.size() > 0 && mFrequencies != null
            && mFrequencies.size() > 0);
  }

  @Override
  public void parse(HttpServletRequest request) {
    parse(getContent(request));
  }

  private void parse(JsonObject jsonObject) {
    if(jsonObject != null) {
      // Core
      mLoanCap = (float) getDoubleFromJson(jsonObject, KEY_LOAN_CAP, 0.0d);
      mAssessment = new AssessmentInfo(jsonObject.getJsonObject(KEY_ASSESSMENT));

      // Amortizations
      mAmortizations = new ArrayList<>();
      JsonArray amortizationsArray = jsonObject.getJsonArray(KEY_AMORTIZATIONS);
      if (amortizationsArray != null) {
        for (int i = 0; i < amortizationsArray.size(); i++) {
          mAmortizations.add(new LoanAmortization(amortizationsArray.getJsonObject(i)));
        }
      }

      // Frequencies
      mFrequencies = new ArrayList<>();
      JsonArray frequenciesArray = jsonObject.getJsonArray(KEY_FREQUENCIES);
      if (frequenciesArray != null) {
        for (int i = 0; i < frequenciesArray.size(); i++) {
          mFrequencies.add(new LoanFrequency(frequenciesArray.getJsonObject(i)));
        }
      }
    }
  }
}
