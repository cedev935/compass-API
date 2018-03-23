package com.gncompass.serverfront.api.parser;

import com.gncompass.serverfront.api.executer.AbstractExecuter;
import com.gncompass.serverfront.api.executer.BorrowerAssessmentCreate;
import com.gncompass.serverfront.api.executer.BorrowerAssessmentFile;
import com.gncompass.serverfront.api.executer.BorrowerAssessmentInfo;
import com.gncompass.serverfront.api.executer.BorrowerAssessments;
import com.gncompass.serverfront.api.executer.BorrowerAssessmentSubmit;
import com.gncompass.serverfront.api.executer.BorrowerCreate;
import com.gncompass.serverfront.api.executer.BorrowerInfo;
import com.gncompass.serverfront.api.executer.BorrowerLogin;
import com.gncompass.serverfront.api.executer.BorrowerLogout;
import com.gncompass.serverfront.api.executer.BorrowerUpdate;
import com.gncompass.serverfront.util.HttpHelper.RequestType;

import java.io.IOException;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.ServletException;

public abstract class BorrowerParser {
  private static final String PATH_ASSESSMENTS = "assessments";
  private static final String PATH_BANKS = "banks";
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
        executer = new BorrowerAssessments(borrowerUuid);
      } else if (type == RequestType.POST) {
        executer = new BorrowerAssessmentCreate(borrowerUuid);
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
        if (type == RequestType.GET) {
          executer = new BorrowerAssessmentInfo(borrowerUuid, assessmentUuid);
        } else if (type == RequestType.POST) {
          executer = new BorrowerAssessmentSubmit(borrowerUuid, assessmentUuid);
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
        executer = new BorrowerAssessmentFile(borrowerUuid, assessmentUuid, assessmentFile);
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
    // TODO!
    return null;
  }
}
