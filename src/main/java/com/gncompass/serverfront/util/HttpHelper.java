package com.gncompass.serverfront.util;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.List;

import javax.json.Json;
import javax.json.JsonObject;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class HttpHelper {
  private static final String JSON_ERROR_CODE = "error_code";
  private static final String JSON_ERROR_STRING = "error_string";

  public enum RequestType {
    DELETE,
    GET,
    POST,
    PUT
  }

  /**
   * Returns the user reference from the request URI made in the request. This assumes it has
   * been validated to be a protected base URI and the expected location of the user UUID is at
   * location /core/v1/<borrowers, investors>/<user reference>/*
   * @param request the HTTP request object
   * @param validateIfUuid TRUE if the user reference should be checked if UUID
   *                       (only required once per request)
   * @return the user reference if found (and optionally validated). NULL if not found
   */
  public static String getUserReference(HttpServletRequest request, boolean validateIfUuid) {
    String requestUri = request.getRequestURI();
    String[] requestSplit = requestUri.split("/");
    if(requestSplit.length >= 5) {
      String userReference = requestSplit[4];
      if(validateIfUuid) {
        if(StringHelper.isUuid(userReference)) {
          return userReference;
        }
      } else {
        return userReference;
      }
    }
    return null;
  }

  /**
   * Parse the full URI and return the chunks in a string list
   * @param request the HTTP servlet request
   * @return the string list of the parameters broken apart
   */
  public static List<String> parseFullUrl(HttpServletRequest request) {
      try {
      String pathAfterContext = request.getRequestURI().substring(
          request.getContextPath().length() + request.getServletPath().length() + 1);
      List<String> chunks = new ArrayList<>();
      for (String val : pathAfterContext.split("/")) {
        chunks.add(URLDecoder.decode(val, "UTF-8"));
      }
      String query = request.getQueryString();
      if (query!=null) {
        for (String val : query.split("&")) {
          chunks.add(URLDecoder.decode(val, "UTF-8"));
        }
      }
      return chunks;
    } catch (UnsupportedEncodingException uee) {
      throw new RuntimeException("Invalid URL encoding", uee);
    }
  }

  /**
   * Sets the http response based on the provided code and the JSON object
   * @param httpResponse the HTTP response object reference
   * @param httpCode the HTTP code of the response. Eg, 200, 401, 403, etc
   * @param response the JSON object response to write
   * @throws IOException throws on failed to access the writer of the HTTP response
   */
  private static void setResponse(HttpServletResponse httpResponse, int httpCode,
                                  JsonObject response) throws IOException {
    httpResponse.setStatus(httpCode);
    if(response != null) {
      httpResponse.setContentType("application/json");
      httpResponse.getWriter().write(response.toString());
    }
  }

  /**
   * Sets the http response based on the provided error code and the JSON object
   * @param httpResponse the HTTP response object reference
   * @param httpCode the HTTP code of the response. Eg, 401, 403, etc
   * @param errorCode the internal error code (inside JSON)
   * @param error the error string description
   * @throws IOException throws on failed to access the writer of the HTTP response
   */
  public static void setResponseError(HttpServletResponse httpResponse, int httpCode, int errorCode,
                                      String error) throws IOException {
    JsonObject response = Json.createObjectBuilder()
        .add(JSON_ERROR_CODE, errorCode)
        .add(JSON_ERROR_STRING, error)
        .build();
    setResponse(httpResponse, httpCode, response);
  }

  /**
   * Sets the http response based on success (200) and the JSON response object (can be null)
   * @param httpResponse the HTTP response object reference
   * @param response the JSON object response to write
   * @throws IOException throws on failed to access the writer of the HTTP response
   */
  public static void setResponseSuccess(HttpServletResponse httpResponse, JsonObject response)
      throws IOException {
    setResponseSuccess(httpResponse, HttpServletResponse.SC_OK, response);
  }

  /**
   * Sets the http response based on a success code and the JSON response object (can be null)
   * @param httpResponse the HTTP response object reference
   * @param httpCode the HTTP code of the response. Eg, 200, 201, etc
   * @param response the JSON object response to write
   * @throws IOException throws on failed to access the writer of the HTTP response
   */
  public static void setResponseSuccess(HttpServletResponse httpResponse, int httpCode,
                                        JsonObject response) throws IOException {
    setResponse(httpResponse, httpCode, response);
  }
}
