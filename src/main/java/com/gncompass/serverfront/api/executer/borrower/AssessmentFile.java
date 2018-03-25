package com.gncompass.serverfront.api.executer.borrower;

import com.gncompass.serverfront.api.executer.AbstractExecuter;
import com.gncompass.serverfront.db.model.Borrower;
import com.gncompass.serverfront.util.HttpHelper;
import com.gncompass.serverfront.util.StringHelper;

import com.google.appengine.api.blobstore.BlobKey;
import com.google.appengine.api.blobstore.BlobstoreService;
import com.google.appengine.api.blobstore.BlobstoreServiceFactory;

import java.io.IOException;

import javax.json.Json;
import javax.json.JsonObject;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.ServletException;

public class AssessmentFile extends AbstractExecuter {
  private String mAssessmentUuid = null;
  private String mBorrowerUuid = null;
  private String mFileName = null;

  public AssessmentFile(String borrowerUuid, String assessmentUuid, String fileName) {
    mAssessmentUuid = assessmentUuid;
    mBorrowerUuid = borrowerUuid;
    mFileName = fileName;
  }

  @Override
  protected void execute(HttpServletResponse response) throws ServletException, IOException {
    boolean next = false;

    // Fetch the borrower
    Borrower borrower = new Borrower().getBorrower(mBorrowerUuid);
    if (borrower != null) {
      next = true;
    } else {
      // This is a server error. Should never fail since this user was authenticated
      HttpHelper.setResponseError(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
          1601, "The borrower information failed to be fetched from the repository");
    }

    // Fetch the assessment file reference
    com.gncompass.serverfront.db.model.AssessmentFile assessmentFile = null;
    if (next) {
      next = false;
      assessmentFile = new com.gncompass.serverfront.db.model.AssessmentFile()
                                                .getFile(borrower, mAssessmentUuid, mFileName);
      if (assessmentFile != null) {
        next = true;
      } else {
        // Assessment file not found
        HttpHelper.setResponseError(response, HttpServletResponse.SC_NOT_FOUND,
            1602, "The assessment file for this borrower could not be found");
      }
    }

    // Fetch the file from the cloud storage
    if (next) {
      BlobstoreService blobstoreService = BlobstoreServiceFactory.getBlobstoreService();
      BlobKey blobKey = blobstoreService.createGsBlobKey(assessmentFile.getGSPath(mAssessmentUuid));
      blobstoreService.serve(blobKey, response);
    }
  }

  @Override
  protected int getInvalidErrorCode() {
    return 1600;
  }

  @Override
  protected String getInvalidErrorString() {
    return "Invalid input for fetching an assessment file";
  }

  @Override
  protected boolean validate(HttpServletRequest request) {
    return (mAssessmentUuid != null && StringHelper.isUuid(mAssessmentUuid)
            && mBorrowerUuid != null && StringHelper.isUuid(mBorrowerUuid)
            && mFileName != null && mFileName.length() > 0);
  }
}
