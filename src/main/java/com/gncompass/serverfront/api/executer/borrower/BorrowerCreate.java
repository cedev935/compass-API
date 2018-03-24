package com.gncompass.serverfront.api.executer.borrower;

import com.gncompass.serverfront.api.executer.AbstractExecuter;
import com.gncompass.serverfront.api.model.RegisterRequest;
import com.gncompass.serverfront.db.model.Borrower;
import com.gncompass.serverfront.db.model.Country;
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

public class BorrowerCreate extends AbstractExecuter {
  private RegisterRequest mRegisterRequest = null;

  public BorrowerCreate() {
    mRegisterRequest = new RegisterRequest();
  }

  @Override
  protected void execute(HttpServletResponse response) throws ServletException, IOException {
    Borrower borrower = new Borrower();
    boolean next = false;

    // First, check if email is existing
    if (!borrower.isEmailExisting(mRegisterRequest.mEmail)) {
      next = true;
    } else {
      HttpHelper.setResponseError(response, HttpServletResponse.SC_CONFLICT,
          1001, "A borrower account already exists with the email provided");
    }

    // Next, fetch the country ID
    Country country = null;
    if (next) {
      next = false;

      country = new Country().getCountry(mRegisterRequest.mCountry);
      if (country != null) {
        next = true;
      } else {
        HttpHelper.setResponseError(response, HttpServletResponse.SC_BAD_REQUEST,
            1000, "The country requested is not supported or active");
      }
    }

    // Next, hash the password, generate the session UUID, and borrower UUID, and attempt to add
    // the new user entry to the database
    UserSession sessionEntry = null;
    if (next) {
      next = false;

      // Generate the information
      String hash = BCrypt.hashpw(mRegisterRequest.mPassword, BCrypt.gensalt());
      UUID deviceUuid = UUID.fromString(mRegisterRequest.mDeviceId);
      UUID referenceUuid = UUID.randomUUID();
      UUID sessionUuid = UUID.randomUUID();

      // Create the borrower and the associated session
      Borrower borrowerEntry = new Borrower(referenceUuid, mRegisterRequest.mEmail,
                                            mRegisterRequest.mName, hash, country);
      sessionEntry = new UserSession(borrowerEntry, deviceUuid, sessionUuid);

      // Attempt to add to the database
      if(sessionEntry.addToDatabaseWithUser()) {
        HttpHelper.setResponseSuccess(response, HttpServletResponse.SC_CREATED,
                                      sessionEntry.getAuthResponse().toJson());
      } else {
        // This is a server error. Should never fail unless UUIDs clash
        HttpHelper.setResponseError(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
            1002, "The new borrower failed to be added based on the generated input");
      }
    }
  }

  @Override
  protected int getInvalidErrorCode() {
    return 1000;
  }

  @Override
  protected String getInvalidErrorString() {
    return "Invalid input for creating a borrower";
  }

  @Override
  protected boolean validate(HttpServletRequest request) {
    mRegisterRequest.parse(request);
    return mRegisterRequest.isValid();
  }
}
