package com.gncompass.serverfront.api.executer;

import com.gncompass.serverfront.api.auth.Session;
import com.gncompass.serverfront.api.model.AuthRequest;
import com.gncompass.serverfront.db.model.Borrower;
import com.gncompass.serverfront.db.model.UserSession;
import com.gncompass.serverfront.util.HttpHelper;

import java.io.IOException;
import java.util.UUID;

import javax.json.Json;
import javax.json.JsonObject;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.ServletException;

import org.mindrot.jbcrypt.BCrypt;

public class BorrowerLogin extends AbstractExecuter {
  private AuthRequest mAuthRequest = null;

  public BorrowerLogin() {
    mAuthRequest = new AuthRequest();
  }

  @Override
  protected void execute(HttpServletResponse response) throws ServletException, IOException {
    boolean next = false;

    // Fetch the borrower based on the email
    Borrower borrower = new Borrower().getBorrowerByEmail(mAuthRequest.mEmail);
    if (borrower != null) {
      next = true;
    } else {
      HttpHelper.setResponseError(response, HttpServletResponse.SC_UNAUTHORIZED,
          1101, "Invalid user or password for logging in to borrower account");
    }

    // If success, check the password
    if (next) {
      next = false;

      if (BCrypt.checkpw(mAuthRequest.mPassword, borrower.mPassword)) {
        next = true;
      } else {
        HttpHelper.setResponseError(response, HttpServletResponse.SC_UNAUTHORIZED,
            1101, "Invalid user or password for logging in to borrower account");
      }
    }

    // If success, make sure that there is no existing session that matches the device UUID
    if (next) {
      // Delete any sessions that match the device ID and the user ID
      UUID deviceUuid = UUID.fromString(mAuthRequest.mDeviceId);
      UserSession.deleteIfMatches(borrower, deviceUuid);

      // Clean out any sessions that exist for this user
      Session.uncacheBorrower(borrower.mReferenceUuid);

      // Add the new session
      UUID sessionUuid = UUID.randomUUID();
      UserSession sessionEntry = new UserSession(borrower, deviceUuid, sessionUuid);
      if(sessionEntry.addToDatabase()) {
        HttpHelper.setResponseSuccess(response, HttpServletResponse.SC_OK,
                                      sessionEntry.getAuthResponse().toJson());
      } else {
        // This is a server error. Should never fail unless UUIDs clash
        HttpHelper.setResponseError(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
            1102, "The new login session failed to be added for the existing borrower");
      }
    }
  }

  @Override
  protected int getInvalidErrorCode() {
    return 1100;
  }

  @Override
  protected String getInvalidErrorString() {
    return "Invalid input for logging into a borrower account";
  }

  @Override
  protected boolean validate(HttpServletRequest request) {
    mAuthRequest.parse(request);
    return mAuthRequest.isValid();
  }
}
