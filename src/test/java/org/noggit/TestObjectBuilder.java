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

import junit.framework.TestCase;

import java.io.IOException;
import java.util.*;

/**
 * @author yonik
 * @version $Id$
 */
public class TestObjectBuilder extends TestCase {

  public void test(String val, Object expected) throws IOException {
    val = val.replace('\'','"');
    Object v = ObjectBuilder.fromJSON(val);

    String s1 = JSONUtil.toJSON(v,-1);
    String s2 = JSONUtil.toJSON(expected,-1);
    assertEquals(s1, s2);

    // not make sure that it round-trips correctly
    JSONParser p2 = TestJSONParser.getParser(s1);
    Object v2 = ObjectBuilder.getVal(p2);
    String s3 = JSONUtil.toJSON(v2,-1);
    assertEquals(s1, s3);
  }

  public static List<Object> L(Object... lst) {
     return Arrays.asList(lst);
  }
  public static Object[] A(Object... lst) {
     return lst;
  }
  public static Map<String, Object> O(Object... lst) {
    LinkedHashMap<String,Object> map = new LinkedHashMap<String,Object>();
    for (int i=0; i<lst.length; i+=2) {
      map.put(lst[i].toString(), lst[i+1]);
    }
    return map;
  }
  public void testWithoutStartingBraces() throws IOException {
    JSONParser parser = new JSONParser("a: {key:val1}b:{key:val2}");
    parser.setFlags(JSONParser.FLAGS_DEFAULT | JSONParser.OPTIONAL_OUTER_BRACES| JSONParser.ALLOW_MISSING_COLON_COMMA_BEFORE_OBJECT);
    ObjectBuilder objectBuilder = new ObjectBuilder(parser);
    String s1 = JSONUtil.toJSON(objectBuilder.getObject(),-1);
    String expected = JSONUtil.toJSON(O("a", O("key", "val1"), "b", O("key", "val2")),-1);
    assertEquals(s1, expected);
  }

  public void testVariations(String str, Object expected) throws IOException {
    test("["+str+"]", L(expected));
    test("["+str+","+str+"]", L(expected, expected));
    test("["+str+","+str+"]", A(expected, expected));
    test("{'foo':"+str+"}", O("foo",expected));
    test("{'foo':"+str+",'bar':{'a':"+str+"},'baz':["+str+"],'zzz':["+str+"]}",
            O("foo",expected,"bar",O("a",expected),"baz", L(expected), "zzz", A(expected)));

  }

  public void testBuilder() throws IOException {
    testVariations("[]", L());
    testVariations("[]", L());
    testVariations("{}", O());
    testVariations("[[]]", L(L()));
    testVariations("{'foo':{}}", O("foo",O()));
    testVariations("[false,true,1,1.4,null,'hi']", L(false, true, 1, 1.4, null, "hi"));
    testVariations("'hello'", "hello".toCharArray());

    // test array types
    testVariations("[[10,20],['a','b']]", L(A(10,20),A("a","b")));
    testVariations("[1,2,3]", new int[]{1,2,3});
    testVariations("[1,2,3]", new long[]{1,2,3});
    testVariations("[1.0,2.0,3.0]", new float[]{1,2,3});
    testVariations("[1.0,2.0,3.0]", new double[]{1,2,3});
    testVariations("[1,2,3]", new short[]{1,2,3});
    testVariations("[1,2,3]", new byte[]{1,2,3});
    testVariations("[false,true,false]", new boolean[]{false,true,false});
  }

}
