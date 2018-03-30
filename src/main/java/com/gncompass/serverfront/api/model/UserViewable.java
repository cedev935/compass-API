package com.gncompass.serverfront.api.model;

import com.gncompass.serverfront.db.model.BankConnection;

import java.util.List;

import javax.json.Json;
import javax.json.JsonArrayBuilder;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;
import javax.servlet.http.HttpServletRequest;

public abstract class UserViewable extends AbstractModel {
  private static final String KEY_ADDRESS1 = "address1";
  private static final String KEY_ADDRESS2 = "address2";
  private static final String KEY_ADDRESS3 = "address3";
  private static final String KEY_BANKS = "banks";
  private static final String KEY_CITY = "city";
  private static final String KEY_COUNTRY = "country";
  private static final String KEY_NAME = "name";
  private static final String KEY_POST_CODE = "post_code";
  private static final String KEY_PROVINCE = "province";

  public String mAddress1 = null;
  public String mAddress2 = null;
  public String mAddress3 = null;
  public String mCity = null;
  public String mCountry = null;
  public String mName = null;
  public String mPostCode = null;
  public String mProvince = null;

  // Send only parameters
  private List<BankConnection> mBankConnections = null;

  @Override
  protected void addToJson(JsonObjectBuilder jsonBuilder) {
    jsonBuilder.add(KEY_ADDRESS1, mAddress1);
    if (mAddress2 != null) {
      jsonBuilder.add(KEY_ADDRESS2, mAddress2);
    }
    if (mAddress3 != null) {
      jsonBuilder.add(KEY_ADDRESS3, mAddress3);
    }
    jsonBuilder.add(KEY_CITY, mCity);
    jsonBuilder.add(KEY_COUNTRY, mCountry);
    jsonBuilder.add(KEY_NAME, mName);
    if (mPostCode != null) {
      jsonBuilder.add(KEY_POST_CODE, mPostCode);
    }
    if (mProvince != null) {
      jsonBuilder.add(KEY_PROVINCE, mProvince);
    }

    if (mBankConnections != null) {
      JsonArrayBuilder bankArrayBuilder = Json.createArrayBuilder();
      for (BankConnection bc : mBankConnections) {
        JsonObjectBuilder objectBuilder = bc.getApiSummary().toJsonBuilder();
        if (objectBuilder != null) {
          bankArrayBuilder.add(objectBuilder);
        }
      }
      jsonBuilder.add(KEY_BANKS, bankArrayBuilder);
    }
  }

  @Override
  public boolean isValid() {
    return (mName != null && mName.length() > 0 &&
            mAddress1 != null &&
            mCity != null &&
            mCountry != null && mCountry.length() == Country.CODE_LENGTH);
  }

  @Override
  public void parse(HttpServletRequest request) {
    JsonObject jsonObject = getContent(request);
    if(jsonObject != null) {
      mAddress1 = jsonObject.getString(KEY_ADDRESS1, null);
      mAddress2 = jsonObject.getString(KEY_ADDRESS2, null);
      mAddress3 = jsonObject.getString(KEY_ADDRESS3, null);
      mCity = jsonObject.getString(KEY_CITY, null);
      mCountry = jsonObject.getString(KEY_COUNTRY, null);
      mName = jsonObject.getString(KEY_NAME, null);
      mPostCode = jsonObject.getString(KEY_POST_CODE, null);
      mProvince = jsonObject.getString(KEY_PROVINCE, null);
    }
  }

  public void setBankData(List<BankConnection> bankConnections) {
    mBankConnections = bankConnections;
  }

  public void setUserData(String name, String address1, String address2, String address3,
                          String city, String province, String postCode, String country) {
    mAddress1 = address1;
    mAddress2 = address2;
    mAddress3 = address3;
    mCity = city;
    mCountry = country;
    mName = name;
    mPostCode = postCode;
    mProvince = province;
  }
}
