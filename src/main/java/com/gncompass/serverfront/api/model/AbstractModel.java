package com.gncompass.serverfront.api.model;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.json.Json;
import javax.json.JsonException;
import javax.json.JsonObject;
import javax.json.JsonReader;
import javax.servlet.http.HttpServletRequest;

public abstract class AbstractModel {
  protected static String CONTENT_JSON = "application/json";

  protected abstract Logger getLogger();
  public abstract boolean isValid();
  public abstract void parse(HttpServletRequest request);

  public JsonObject getContent(HttpServletRequest request) {
    if (isContentJson(request)) {
      try (InputStream inputStream = request.getInputStream()) {
        try (JsonReader jsonReader = Json.createReader(inputStream)) {
          return jsonReader.readObject();
        } catch (JsonException je) {
          // Just warn and fall through
          getLogger().log(Level.WARNING, "Failed to parse the JSON content request body", je);
        }
      } catch (IOException ie) {
        // Just warn and fall through
        getLogger().log(Level.WARNING, "Failed to fetch stream of content request body", ie);
      }
    }
    return null;
  }

  public boolean isContentJson(HttpServletRequest request) {
    String contentType = request.getContentType();
    return (contentType != null && contentType.equals(CONTENT_JSON)
            && request.getContentLength() > 0);
  }
}
