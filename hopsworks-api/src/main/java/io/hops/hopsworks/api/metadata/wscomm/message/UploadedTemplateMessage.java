/*
 * Copyright (C) 2013 - 2018, Logical Clocks AB and RISE SICS AB. All rights reserved
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this
 * software and associated documentation files (the "Software"), to deal in the Software
 * without restriction, including without limitation the rights to use, copy, modify, merge,
 * publish, distribute, sublicense, and/or sell copies of the Software, and to permit
 * persons to whom the Software is furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or
 * substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS  OR IMPLIED, INCLUDING
 * BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
 * NONINFRINGEMENT. IN NO EVENT SHALL  THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM,
 * DAMAGES OR  OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 *
 */

package io.hops.hopsworks.api.metadata.wscomm.message;

import io.hops.hopsworks.common.dao.metadata.EntityIntf;
import java.io.StringReader;
import java.util.List;
import java.util.logging.Logger;
import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;

/**
 * Represents the message produced by an uploaded file
 */
public class UploadedTemplateMessage extends ContentMessage {

  private static final Logger logger = Logger.getLogger(
          UploadedTemplateMessage.class.getName());

  private String templateName;
  private JsonArray templateContents;

  public UploadedTemplateMessage() {
    super();
    this.TYPE = "UploadedTemplateMessage";
  }

  @Override
  public void init(JsonObject json) {
    this.sender = json.getString("sender");
    this.message = json.getString("message");
    this.action = json.getString("action");
    super.setAction(this.action);
  }

  @Override
  public String encode() {
    String value = Json.createObjectBuilder()
            .add("sender", this.sender)
            .add("type", this.TYPE)
            .add("status", this.status)
            .add("message", this.message)
            .build()
            .toString();

    return value;
  }

  /**
   * Produces a TemplateMessage initialized with a template name ready to be
   * persisted in the database
   * <p/>
   * @return
   */
  public Message addNewTemplateMessage() {
    JsonObjectBuilder builder = Json.createObjectBuilder();
    builder.add("templateName", this.templateName);

    String msg = builder.build().toString();

    TemplateMessage tempmesg = new TemplateMessage();
    tempmesg.setAction("add_new_template");
    tempmesg.setMessage(msg);

    return tempmesg;
  }

  /**
   * Produces a TemplateMessage initialized with the template contents ready to
   * be persisted in the database
   * <p/>
   * @param templateId
   * @return
   */
  public Message addNewTemplateContentMessage(int templateId) {
    JsonObjectBuilder outerJson = Json.createObjectBuilder();
    JsonObjectBuilder innerJson = Json.createObjectBuilder();
    outerJson.add("tempid", templateId);

    innerJson.add("name", "MainBoard");
    innerJson.add("numberOfColumns", 3);
    innerJson.add("columns", this.templateContents);

    outerJson.add("bd", innerJson);

    String msg = outerJson.build().toString();

    TemplateMessage tempmesg = new TemplateMessage();
    tempmesg.setAction("extend_template");
    tempmesg.setTemplateid(templateId);
    tempmesg.setMessage(msg);

    return tempmesg;
  }

  @Override
  public String getAction() {
    return this.action;
  }

  @Override
  public List<EntityIntf> parseSchema() {
    JsonObject obj = Json.createReader(new StringReader(this.message)).
            readObject();

    //extract the template name out of the message
    this.templateName = obj.getString("templateName");

    //extract the template contents out of the message
    this.templateContents = obj.getJsonArray("templateContents");

    //no need to return a list with entities
    return null;
  }

  @Override
  public String getMessage() {
    return this.message;
  }

  @Override
  public void setMessage(String msg) {
    this.message = msg;
  }

  @Override
  public String getSender() {
    return this.sender;
  }

  @Override
  public void setSender(String sender) {
    this.sender = sender;
  }

  @Override
  public String getStatus() {
    return this.status;
  }

  @Override
  public void setStatus(String status) {
    this.status = status;
  }

  @Override
  public String toString() {
    return "{\"sender\": \"" + this.sender + "\", "
            + "\"type\": \"" + this.TYPE + "\", "
            + "\"status\": \"" + this.status + "\", "
            + "\"action\": \"" + this.action + "\", "
            + "\"message\": \"" + this.message + "\"}";
  }

}
