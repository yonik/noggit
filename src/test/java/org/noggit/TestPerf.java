/**
 *  Copyright 2006- Yonik Seeley
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.noggit;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/* mvn package
 * java -classpath ./target/test-classes/:./target/noggit-0.7-SNAPSHOT.jar org.noggit.TestPerf -i 1000000
 */
public class TestPerf {
  static int flags = JSONParser.FLAGS_DEFAULT;
  static int iter = 1;
  static  List<String> vals = new ArrayList<String>();
  static  List<Object> objs = new ArrayList<Object>();
  static boolean ws = false;
  static boolean nows = false;
  static boolean writer = false;

  public static void main(String[] args) throws Exception {
    int i=0;
    while (i<args.length) {
      String k=args[i++];
      if ("-i".equals(k)) {
        iter = Integer.parseInt(args[i++]);
      } else if ("-json".equals(k)) {
        vals.add( args[i++] );
      } else if ("-ws".equals(k)) {
        // convert values to add more whitespace
        ws = true;
      } else if ("-nows".equals(k)) {
        // convert values to eliminate whitespace.  this can be used with -ws and both values will be used.
        nows = true;
      } else if ("-flags".equals(k)) {
        Integer.parseInt(k);
      } else if ("-writer".equals(k)) {
        writer = true;
      }
    }

    if (vals.size() == 0) {
      vals.add( "[ 1, 2, 3, 4, 5 ]");
      vals.add( "[ \"1\", \"2\", \"3\", \"4\", \"5\" ] ");
      vals.add( "{ \"k1\":\"val1\", \"k2\":\"val2\", \"k3\":\"val3\", \"k4\":\"val4\", \"k5\":\"val5\" }" );
      vals.add( "{\"a\":{\"b\":{\"c\":{\"d\":{\"e\":{\"f\":{\"g\":{\"h\":{\"i\":{\"j\":{}}}}}}}}}}}" );
      vals.add( "[1,[2,[3,[4,[5,[true],[6],[7],8],[9,[10,[[null,[false]]],11]]]]]]" );
      vals.add( "{\"foo\":10, \"bar\":20, \"baz\":{ \"lst\" : [ 5000, {\"first_name\" : \"Yonik\", \"last_name\" : \"Seeley\", \"address\" : \"1234 somewhere lane, some state, 54321\", \"id\" : 123454321}]}}" );
      vals.add( "["
              + "{ \"k1\":true,         \"k2\":\"val2a\", \"k3\":\"val3a\", \"k4\":\"val4a\", \"k5\":\"val5a\" },"
              + "{ \"k1\":false,        \"k2\":\"val2b\", \"k3\":\"val3b\", \"k4\":\"val4b\", \"k5\":\"val5b\" },"
              + "{ \"k1\":null,         \"k2\":\"val2c\", \"k3\":\"val3c\", \"k4\":\"val4c\", \"k5\":\"val5c\" },"
              + "{ \"k1\":1.414213562,  \"k2\":\"val2d\", \"k3\":\"val3d\", \"k4\":\"val4d\", \"k5\":\"val5d\" },"
              + "{ \"k1\":123456789,    \"k2\":\"val2e\", \"k3\":\"val3e\", \"k4\":\"val4e\", \"k5\":\"val5e\" }"
              +"]" );


      // make a big value of all the previous values
      StringBuilder sb = new StringBuilder("[");
      boolean first = true;
      for (String val : vals) {
        if (first) {
          first = false;
        } else {
          sb.append(",\n");
        }
        sb.append(val);
      }
      sb.append("]");
      vals.add(sb.toString());
    }



    // handle adding or removing whitespace
    if (ws || nows || writer) {
      List<String> out = new ArrayList<String>();
      for (String val : vals) {
        Object o = ObjectBuilder.fromJSON(val);
        if (writer) {
          objs.add(o);
        } else if (ws) {
          String s = JSONUtil.toJSON(o, 2);
          out.add(s);
        } else if (nows) {
          String s = JSONUtil.toJSON(o, -1);
          out.add(s);
        }
      }
      if (!writer)
        vals = out;
    }

    // calculate total size per iteration
    int sz = 0;
    for (String val : vals) {
      sz += val.length();
    }

    long start = System.currentTimeMillis();

    int ret=0;
    for (int j=0; j<iter; j++) {
      if (writer) {
        for (Object o : objs) {
          ret += write(o);
        }
      } else {
        for (String json : vals) {
          ret += parse(json);
        }
      }
    }

    long end = System.currentTimeMillis();
    System.out.println("Result:"+ret+", TIME="+(end-start) + "\t\t\tITER_SZ=" + sz + " \tMEGACHARS/SEC=" + ( ((long)iter) * sz / (1000*(end - start)) ) );
  }

  public static int parse(String val) throws IOException {
    JSONParser parser = new JSONParser(val);
    parser.setFlags(flags);
    return parse(parser);
  }

  public static int parse(JSONParser parser) throws IOException {
    int ret = 0;
    for (;;) {
      int ev = parser.nextEvent();
      switch (ev) {
        case JSONParser.STRING: ret += parser.getStringChars().length(); break;
        case JSONParser.BOOLEAN: ret += parser.getBoolean() ? 1 : 2; break;
        case JSONParser.BIGNUMBER: ret += parser.getNumberChars().length(); break;
        case JSONParser.NUMBER: ret += Double.doubleToRawLongBits(parser.getDouble()); break;
        case JSONParser.ARRAY_START: ret += 13; break;
        case JSONParser.ARRAY_END: ret += 17; break;
        case JSONParser.OBJECT_START: ret += 19; break;
        case JSONParser.OBJECT_END: ret += 23; break;
        default: // ret += ev;
      }
      if (ev == JSONParser.EOF) break;
    }

    return ret;
  }

  static CharArr out = new CharArr();
  public static int write(Object o) {
    out.reset();
    JSONWriter writer = new JSONWriter(out,-1);
    writer.write(o);
    return out.size();
  }
}
