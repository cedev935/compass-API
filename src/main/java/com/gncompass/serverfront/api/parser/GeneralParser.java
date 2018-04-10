package com.gncompass.serverfront.api.parser;

import com.gncompass.serverfront.api.executer.AbstractExecuter;
import com.gncompass.serverfront.api.executer.CountriesGet;
import com.gncompass.serverfront.api.executer.CountryBanksGet;
import com.gncompass.serverfront.api.executer.LoanAmortizationsGet;
import com.gncompass.serverfront.api.executer.LoanFrequenciesGet;
import com.gncompass.serverfront.api.executer.UploadedAssessmentFile;
import com.gncompass.serverfront.util.HttpHelper.RequestType;

import java.io.IOException;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.ServletException;

public abstract class GeneralParser {
  private static final String FUNCTION_COUNTRIES = "countries";
  private static final String FUNCTION_LOANS = "loans";
  private static final String FUNCTION_UPLOADS = "uploads";
  private static final String TYPE_AMORTIZATIONS = "amortizations";
  private static final String TYPE_ASSESSMENTS = "assessments";
  private static final String TYPE_BANKS = "banks";
  private static final String TYPE_FREQUENCIES = "frequencies";

  public static void parseRequest(String function, List<String> pathChunks, RequestType type,
                                  HttpServletRequest request, HttpServletResponse response)
      throws ServletException, IOException {
    AbstractExecuter executer = null;

    // General: /{function}/_
    switch (function) {
      case FUNCTION_COUNTRIES:
        if (pathChunks.size() == 0) {
          if (type == RequestType.GET) {
            executer = new CountriesGet();
          }
        } else if (pathChunks.size() == 2) {
          if (pathChunks.get(1).equals(TYPE_BANKS)) {
            if (type == RequestType.GET) {
              executer = new CountryBanksGet(pathChunks.get(0));
            }
          }
        }
        break;
      case FUNCTION_LOANS:
        if (pathChunks.size() == 1) {
          String loansType = pathChunks.get(0);
          if (loansType.equals(TYPE_AMORTIZATIONS)) {
            if (type == RequestType.GET) {
              executer = new LoanAmortizationsGet();
            }
          } else if (loansType.equals(TYPE_FREQUENCIES)) {
            if (type == RequestType.GET) {
              executer = new LoanFrequenciesGet();
            }
          }
        }
        break;
      case FUNCTION_UPLOADS:
        if (pathChunks.size() == 2) {
          if (pathChunks.get(0).equals(TYPE_ASSESSMENTS)) {
            if (type == RequestType.POST) {
              executer = new UploadedAssessmentFile(pathChunks.get(1));
            }
          }
        }
        break;
    }

    // Process the execution
    if (executer != null) {
      executer.process(request, response);
    } else {
      response.setStatus(HttpServletResponse.SC_NOT_FOUND);
    }
  }
}
