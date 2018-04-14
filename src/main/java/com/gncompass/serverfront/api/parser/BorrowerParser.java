package com.gncompass.serverfront.api.parser;

import com.gncompass.serverfront.api.executer.AbstractExecuter;
import com.gncompass.serverfront.api.executer.borrower.AssessmentApproved;
import com.gncompass.serverfront.api.executer.borrower.AssessmentCreate;
import com.gncompass.serverfront.api.executer.borrower.AssessmentFile;
import com.gncompass.serverfront.api.executer.borrower.AssessmentInfo;
import com.gncompass.serverfront.api.executer.borrower.AssessmentList;
import com.gncompass.serverfront.api.executer.borrower.AssessmentSubmit;
import com.gncompass.serverfront.api.executer.borrower.BankCreate;
import com.gncompass.serverfront.api.executer.borrower.BankInfo;
import com.gncompass.serverfront.api.executer.borrower.BankList;
import com.gncompass.serverfront.api.executer.borrower.BorrowerCreate;
import com.gncompass.serverfront.api.executer.borrower.BorrowerInfo;
import com.gncompass.serverfront.api.executer.borrower.BorrowerLogin;
import com.gncompass.serverfront.api.executer.borrower.BorrowerLogout;
import com.gncompass.serverfront.api.executer.borrower.BorrowerUpdate;
import com.gncompass.serverfront.api.executer.borrower.LoanList;
import com.gncompass.serverfront.util.HttpHelper.RequestType;

import java.io.IOException;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.ServletException;

public abstract class BorrowerParser {
  private static final String PATH_APPROVED = "approved";
  private static final String PATH_ASSESSMENTS = "assessments";
  private static final String PATH_BANKS = "banks";
  private static final String PATH_LOANS = "loans";
  private static final String PATH_LOGIN = "login";
  private static final String PATH_LOGOUT = "logout";
  public static final String PATH_MAIN = "borrowers";

  /**
   * General parse request start point for the borrower functionality
   * @param pathChunks the separated list of the path
   * @param type the type of request (GET, POST, etc)
   * @param request the request data received
   * @param response the response data to return
   */
  public static void parseRequest(List<String> pathChunks, RequestType type,
                                  HttpServletRequest request, HttpServletResponse response)
      throws ServletException, IOException {
    AbstractExecuter executer = null;
    boolean nextLevel = false;

    // Level 1: /borrowers
    if (pathChunks.size() == 0) {
      if (type == RequestType.POST) {
        executer = new BorrowerCreate();
      }
    } else {
      nextLevel = true;
    }

    // Level 2: /borrowers/{chunk}
    String level2Chunk = null;
    if (nextLevel) {
      nextLevel = false;
      level2Chunk = pathChunks.remove(0);

      switch (level2Chunk) {
        case PATH_LOGIN:
          if (pathChunks.size() == 0 && type == RequestType.POST) {
            executer = new BorrowerLogin();
          }
          break;
        default:
          if (pathChunks.size() == 0) {
            if (type == RequestType.GET) {
              executer = new BorrowerInfo(level2Chunk);
            } else if (type == RequestType.PUT) {
              executer = new BorrowerUpdate(level2Chunk);
            }
          } else {
            nextLevel = true;
          }
          break;
      }
    }

    // Level 3: /borrowers/{UUID}/{chunk}
    if (nextLevel) {
      nextLevel = false;

      switch (pathChunks.remove(0)) {
        case PATH_ASSESSMENTS:
          executer = parseRequestForAssessments(level2Chunk, pathChunks, type, request);
          break;
        case PATH_BANKS:
          executer = parseRequestForBanks(level2Chunk, pathChunks, type, request);
          break;
        case PATH_LOANS:
          executer = parseRequestForLoans(level2Chunk, pathChunks, type, request);
          break;
        case PATH_LOGOUT:
          if (pathChunks.size() == 0 && type == RequestType.POST) {
            executer = new BorrowerLogout(level2Chunk);
          }
          break;
      }
    }

    // Process the execution
    if (executer != null) {
      executer.process(request, response);
    } else {
      response.setStatus(HttpServletResponse.SC_NOT_FOUND);
    }
  }

  /**
   * Parse request for the borrower assessments section. Separated from the main for clarity
   * @param borrowerUuid the borrower reference
   * @param pathChunks the separated list of the path
   * @param type the type of request (GET, POST, etc)
   * @param request the request data received
   * @return the abstract executer to call for the path requested. NULL if not found
   */
  private static AbstractExecuter parseRequestForAssessments(String borrowerUuid,
                                                             List<String> pathChunks,
                                                             RequestType type,
                                                             HttpServletRequest request) {
    AbstractExecuter executer = null;
    boolean nextLevel = false;

    // Level 1: /assessments
    if (pathChunks.size() == 0) {
      if (type == RequestType.GET) {
        executer = new AssessmentList(borrowerUuid);
      } else if (type == RequestType.POST) {
        executer = new AssessmentCreate(borrowerUuid);
      }
    } else {
      nextLevel = true;
    }

    // Level 2: /assessments/{assessmentUuid}
    String assessmentUuid = null;
    if (nextLevel) {
      nextLevel = false;
      assessmentUuid = pathChunks.remove(0);

      if (pathChunks.size() == 0) {
        if (assessmentUuid.equals(PATH_APPROVED)) {
          if (type == RequestType.GET) {
            executer = new AssessmentApproved(borrowerUuid);
          }
        } else if (type == RequestType.GET) {
          executer = new AssessmentInfo(borrowerUuid, assessmentUuid);
        } else if (type == RequestType.POST) {
          executer = new AssessmentSubmit(borrowerUuid, assessmentUuid);
        }
      } else {
        nextLevel = true;
      }
    }

    // Level 3: /assessments/{assessmentUuid}/{assessmentFile}
    if (nextLevel) {
      nextLevel = false;
      String assessmentFile = pathChunks.remove(0);

      if (pathChunks.size() == 0 && type == RequestType.GET) {
        executer = new AssessmentFile(borrowerUuid, assessmentUuid, assessmentFile);
      }
    }

    return executer;
  }

  /**
   * Parse request for the borrower banks section. Separated from the main for clarity
   * @param borrowerUuid the borrower reference
   * @param pathChunks the separated list of the path
   * @param type the type of request (GET, POST, etc)
   * @param request the request data received
   * @return the abstract executer to call for the path requested. NULL if not found
   */
  private static AbstractExecuter parseRequestForBanks(String borrowerUuid,
                                                       List<String> pathChunks,
                                                       RequestType type,
                                                       HttpServletRequest request) {
    AbstractExecuter executer = null;
    boolean nextLevel = false;

    // Level 1: /banks
    if (pathChunks.size() == 0) {
      if (type == RequestType.GET) {
        executer = new BankList(borrowerUuid);
      } else if (type == RequestType.POST) {
        executer = new BankCreate(borrowerUuid);
      }
    } else {
      nextLevel = true;
    }

    // Level 2: /banks/{bankUuid}
    String bankUuid = null;
    if (nextLevel) {
      nextLevel = false;
      bankUuid = pathChunks.remove(0);

      if (pathChunks.size() == 0) {
        if (type == RequestType.GET) {
          executer = new BankInfo(borrowerUuid, bankUuid);
        }
      }
    }

    return executer;
  }

  /**
   * Parse request for the borrower loans section. Separated from the main for clarity
   * @param borrowerUuid the borrower reference
   * @param pathChunks the separated list of the path
   * @param type the type of request (GET, POST, etc)
   * @param request the request data received
   * @return the abstract executer to call for the path requested. NULL if not found
   */
  private static AbstractExecuter parseRequestForLoans(String borrowerUuid,
                                                       List<String> pathChunks,
                                                       RequestType type,
                                                       HttpServletRequest request) {
    AbstractExecuter executer = null;
    boolean nextLevel = false;

    // Level 1: /loans
    if (pathChunks.size() == 0) {
      if (type == RequestType.GET) {
        executer = new LoanList(borrowerUuid);
      } else if (type == RequestType.POST) {
        //executer = new LoanCreate(borrowerUuid);
      }
    } else {
      nextLevel = true;
    }

    // Level 2: /loans/{loanUuid}
    String loanUuid = null;
    if (nextLevel) {
      nextLevel = false;
      loanUuid = pathChunks.remove(0);

      if (pathChunks.size() == 0) {
        if (type == RequestType.GET) {
          //executer = new LoanInfo(borrowerUuid, loanUuid);
        }
      }
    }

    return executer;
  }
}
