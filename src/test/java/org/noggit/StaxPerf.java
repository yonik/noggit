/**
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.noggit;

/**
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
**/

/**
 * @author yonik
 * @version $Id: StaxPerf.java 479919 2006-11-28 05:53:55Z yonik $
 */
public class StaxPerf {

  /*** requires a stax parser or Java6
  static XMLInputFactory factory = XMLInputFactory.newInstance();

  public static int parseXML(String s) throws XMLStreamException {
    XMLStreamReader sr = factory.createXMLStreamReader(new StringReader(s));
    char[] buf = new char[80];    // more efficient retrieval of strings

    for(;;) {
      int ev = sr.next();
      switch (ev) {
        case XMLStreamConstants.END_DOCUMENT: return -1;
        case XMLStreamConstants.START_ELEMENT: return sr.getLocalName().charAt(0);
        case XMLStreamConstants.CHARACTERS:
          int len = sr.getTextCharacters(0,buf,0,80);
          return len>0 ? buf[0] : -1;
        default: break;
      }
    }
  }

  public static int parseJSON(String s) throws IOException {
    JSONParser sr = new JSONParser(new StringReader(s));
    for(;;) {
      int ev = sr.nextEvent();
      switch (ev) {
        case JSONParser.EOF: return 1;
        case JSONParser.STRING: return sr.getStringChars().read();  // first char
        case JSONParser.LONG: return (int)sr.getLong();
        default: break;
      }
    }
  }


  public static void main(String[] argv) throws Exception {
    int iter = Integer.parseInt(argv[0]);

    String[] xml = {
      "<arr> <int>1</int> <int>2</int> <int>3</int> <int>4</int> <int>5</int> </arr>",
      "<arr> <str>1</str> <str>2</str> <str>3</str> <str>4</str> <str>5</str> </arr>",
      "<map> <k1>val1</k1> <k2>val2</k2> <k3>val3</k3> <k4>val4</k4> <k5>val5</k5> </map>",
      "<a><b><c><d><e><f><g><h><i><j>big nesting</j></i></h></g></f></e></d></c></b></a>",
      "<bigarr>"
        + "<map> <k1>val1a</k1> <k2>val2a</k2> <k3>val3a</k3> <k4>val4a</k4> <k5>val5a</k5> </map>"
        + "<map> <k1>val1b</k1> <k2>val2b</k2> <k3>val3b</k3> <k4>val4b</k4> <k5>val5b</k5> </map>"
        + "<map> <k1>val1c</k1> <k2>val2c</k2> <k3>val3c</k3> <k4>val4c</k4> <k5>val5c</k5> </map>"
        + "<map> <k1>val1d</k1> <k2>val2d</k2> <k3>val3d</k3> <k4>val4d</k4> <k5>val5d</k5> </map>"
        + "<map> <k1>val1e</k1> <k2>val2e</k2> <k3>val3e</k3> <k4>val4e</k4> <k5>val5e</k5> </map>"
        + "</bigarr>"
    };

    String[] json = {
      "[ 1, 2, 3, 4, 5 ]",
      "[ \"1\", \"2\", \"3\", \"4\", \"5\" ] ",
      "{ \"k1\":\"val1\", \"k2\":\"val2\", \"k3\":\"val3\", \"k4\":\"val4\", \"k5\":\"val5\" }",
      "{\"a\":{\"b\":{\"c\":{\"d\":{\"e\":{\"f\":{\"g\":{\"h\":{\"i\":{\"j\":{}}}}}}}}}}}",
      "["
        + "{ \"k1\":\"val1a\", \"k2\":\"val2a\", \"k3\":\"val3a\", \"k4\":\"val4a\", \"k5\":\"val5a\" },"
        + "{ \"k1\":\"val1b\", \"k2\":\"val2b\", \"k3\":\"val3b\", \"k4\":\"val4b\", \"k5\":\"val5b\" },"
        + "{ \"k1\":\"val1c\", \"k2\":\"val2c\", \"k3\":\"val3c\", \"k4\":\"val4c\", \"k5\":\"val5c\" },"
        + "{ \"k1\":\"val1d\", \"k2\":\"val2d\", \"k3\":\"val3d\", \"k4\":\"val4d\", \"k5\":\"val5d\" },"
        + "{ \"k1\":\"val1e\", \"k2\":\"val2e\", \"k3\":\"val3e\", \"k4\":\"val4e\", \"k5\":\"val5e\" }"
       +"]",
    };

    long start = System.currentTimeMillis();

    int s = 0;
    int ret=0;
    for (int i=0; i<iter; i++) {
      if (s>=xml.length) s=0;
      // String val = xml[s];
      // System.out.println(val);
      // ret += parseXML(val);
      String val = json[s];
      ret += parseJSON(val);
      s++;
    }


    long end = System.currentTimeMillis();
    System.out.println("Result:"+ret+", TIME="+(end-start));
  }
  ***/

}
