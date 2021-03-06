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

package io.hops.hopsworks.common.util;

import java.io.StringReader;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonObject;
import javax.json.JsonValue;
import io.hops.hopsworks.common.dao.metadata.EntityIntf;
import io.hops.hopsworks.common.dao.metadata.InodeTableComposite;
import io.hops.hopsworks.common.dao.metadata.Metadata;
import io.hops.hopsworks.common.dao.metadata.RawData;

public class JsonUtil {

  private static final Logger logger = Logger.
          getLogger(JsonUtil.class.getName());

  static public InodeTableComposite parseSchemaHeader(String message) {
    JsonObject obj = Json.createReader(new StringReader(message)).readObject();
    InodeTableComposite itc = null;

    try {
      int inodePid = obj.getInt("inodepid");
      String inodeName = obj.getString("inodename");
      int tableid = obj.getInt("tableid");

      itc = new InodeTableComposite(tableid, inodePid,
              inodeName);
    } catch (NullPointerException e) {
      logger.log(Level.SEVERE,
              "Inodepid or inodename or tableid not present in the message");
    }
    return itc;
  }

  static public List<EntityIntf> parseSchemaPayload(String message) {
    JsonObject obj = Json.createReader(new StringReader(message)).readObject();

    JsonObject meta = obj.getJsonObject("metadata");
    List<EntityIntf> data = new LinkedList<>();

    Set<Map.Entry<String, JsonValue>> set = meta.entrySet();
    /*
     * processes a json string of the following format:
     * "{"1":"singleTwo","2":[{"value":"multiOne"},{"value":"multiThree"}],"1025":"55","inodeid":5,"tableid":1}"}
     *
     * Numbers are field ids and the strings are the values they carry. Values
     * may be single or multi strings
     */
    for (Map.Entry<String, JsonValue> entry : set) {

      //avoid the inodeid field as it has been accessed previously
      //keys are the field ids
      if (isNumeric(entry.getKey())) {
        RawData raw = new RawData();
        List<Metadata> metalist = new LinkedList<>();

        //set the field id
        int fieldid = Integer.parseInt(entry.getKey());
        raw.getRawdataPK().setFieldid(fieldid);

        if (entry.getValue().getValueType() == JsonValue.ValueType.STRING) {
          //set the actual metadata
          Metadata metadata = new Metadata();
          metadata.getMetadataPK().setFieldid(fieldid);
          metadata.setData(entry.getValue().toString().replaceAll("\"", ""));
          metalist.add(metadata);

          raw.setMetadata(metalist);
        } else if (entry.getValue().getValueType() == JsonValue.ValueType.ARRAY) {
          /*
           * build a json array out of the string and then iterate through it
           * to get the actual values
           */
          JsonArray array = Json.
                  createReader(new StringReader(entry.getValue().toString())).
                  readArray();
          /*
           * in case the multiselect values are not there, it means the user
           * didn't select anything so skip this part.
           * Avoids adding en empty line to the metadata table
           */
          if (array.size() == 0) {
            continue;
          }

          //scan the array and extract the values
          for (JsonValue value : array) {
            JsonObject object = Json.
                    createReader(new StringReader(value.toString())).
                    readObject();
            //set the actual metadata
            Metadata metadata = new Metadata();
            metadata.getMetadataPK().setFieldid(fieldid);
            metadata.setData(object.getString("value").replaceAll("\"", ""));
            metalist.add(metadata);
          }
          raw.setMetadata(metalist);
        }
        data.add(raw);
      }
    }

    return data;
  }

  private static boolean isNumeric(String value) {
    try {
      Double.parseDouble(value);
    } catch (NumberFormatException e) {
      return false;
    }
    return true;
  }

}
