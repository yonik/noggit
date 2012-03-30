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

import junit.framework.TestCase;

import java.util.Random;
import java.io.StringReader;
import java.io.IOException;

/**
 * @author yonik
 * @version $Id: TestJSONParser.java 1099557 2011-05-04 18:54:26Z yonik $
 */
public class TestJSONParser extends TestCase {

  public static Random r = new Random();

  // these are to aid in debugging if an unexpected error occurs
  static int parserType;
  static int bufferSize;
  static String parserInput;
  static JSONParser lastParser;

  public static String lastParser() {
    return "parserType=" + parserType
            + (parserType==1 ? " bufferSize=" + bufferSize : "")
            + " parserInput='" + parserInput + "'";
  }

  public static JSONParser getParser(String s) {
    return getParser(s, r.nextInt(2), -1);
  }
  
  public static JSONParser getParser(String s, int type, int bufSize) {
    parserInput = s;
    parserType = type;

    JSONParser parser=null;
    switch (type) {
      case 0:
        // test directly using input buffer
        parser = new JSONParser(s.toCharArray(),0,s.length());
        break;
      case 1:
        // test using Reader...
        // small input buffers can help find bugs on boundary conditions

        if (bufSize < 1) bufSize = r.nextInt(25) + 1;
        bufferSize = bufSize;// record in case there is an error
        parser = new JSONParser(new StringReader(s), new char[bufSize]);
        break;
    }
    return lastParser = parser;
  }

  /** for debugging purposes
  public void testSpecific() throws Exception {
    JSONParser parser = getParser("[0",1,1);
    for (;;) {
      int ev = parser.nextEvent();
      if (ev == JSONParser.EOF) {
        break;
      } else {
        System.out.println("got " + JSONParser.getEventString(ev));
      }
    }
  }
  **/

  public static byte[] events = new byte[256];
  static {
    events['{'] = JSONParser.OBJECT_START;
    events['}'] = JSONParser.OBJECT_END;
    events['['] = JSONParser.ARRAY_START;
    events[']'] = JSONParser.ARRAY_END;
    events['s'] = JSONParser.STRING;
    events['b'] = JSONParser.BOOLEAN;
    events['l'] = JSONParser.LONG;
    events['n'] = JSONParser.NUMBER;
    events['N'] = JSONParser.BIGNUMBER;
    events['0'] = JSONParser.NULL;
    events['e'] = JSONParser.EOF;
  }

  // match parser states with the expected states
  public static void parse(JSONParser p, String input, String expected) throws IOException {
    expected += "e";
    for (int i=0; i<expected.length(); i++) {
      int ev = p.nextEvent();
      int expect = events[expected.charAt(i)];
      if (ev != expect) {
        TestCase.fail("Expected " + expect + ", got " + ev
                + "\n\tINPUT=" + input
                + "\n\tEXPECTED=" + expected
                + "\n\tAT=" + i + " ("+ expected.charAt(i) + ")");
      }
    }
  }

  public static void parse(String input, String expected) throws IOException {
    input = input.replace('\'','"');
    for (int i=0; i<Integer.MAX_VALUE; i++) {
      JSONParser p = getParser(input,i,-1);
      if (p==null) break;
      parse(p,input,expected);
    }

    testCorruption(input, 100000);

  }

  public static void testCorruption(String input, int iter) {
    char[] arr = new char[input.length()];

    for (int i=0; i<iter; i++) {
      input.getChars(0, arr.length, arr, 0);
      int changes = r.nextInt(arr.length>>1) + 1;
      for (int j=0; j<changes; j++) {
        char ch;
        switch (r.nextInt(30)) {
          case 0: ch = 0; break;
          case 1: ch = '['; break;
          case 2: ch = ']'; break;
          case 3: ch = '{'; break;
          case 4: ch = '}'; break;
          case 5: ch = '"'; break;
          case 6: ch = '\''; break;
          case 7: ch = ' '; break;
          case 8: ch = '\r'; break;
          case 9: ch = '\n'; break;
          case 10:ch = '\t'; break;
          case 11:ch = ','; break;
          case 12:ch = ':'; break;
          case 13:ch = '.'; break;
          case 14:ch = 'a'; break;
          case 15:ch = 'e'; break;
          case 16:ch = '0'; break;
          case 17:ch = '1'; break;
          case 18:ch = '+'; break;
          case 19:ch = '-'; break;
          case 20:ch = 't'; break;
          case 21:ch = 'f'; break;
          case 22:ch = 'n'; break;
          case 23:ch = '/'; break;
          case 24:ch = '\\'; break;
          case 25:ch = 'u'; break;
          default:ch = (char)r.nextInt(256);
        }

        arr[r.nextInt(arr.length)] = ch;
      }


      JSONParser parser = getParser(new String(arr));
      int ret = 0;
      try {
        for (;;) {
          int ev = parser.nextEvent();
          if (r.nextBoolean()) {
            // see if we can read the event
            switch (ev) {
              case JSONParser.STRING: ret += parser.getString().length(); break;
              case JSONParser.BOOLEAN: ret += parser.getBoolean() ? 1 : 2; break;
              case JSONParser.BIGNUMBER: ret += parser.getNumberChars().length(); break;
              case JSONParser.NUMBER: ret += parser.getDouble(); break;
              case JSONParser.LONG: ret += parser.getLong(); break;
              default: ret += ev;
            }
          }

          if (ev == JSONParser.EOF) break;
        }
      } catch (IOException ex) {
        // shouldn't happen
        System.out.println(ret);  // use ret
      } catch (JSONParser.ParseException ex) {
        // OK
      } catch (Throwable ex) {
        ex.printStackTrace();
        System.out.println(lastParser());
        throw new RuntimeException(ex);
      }
    }
  }


  
  public static class Num {
    public String digits;
    public Num(String digits) {
      this.digits = digits;
    }
    public String toString() { return new String("NUMBERSTRING("+digits+")"); }
    public boolean equals(Object o) {
      return (getClass()==o.getClass() && digits.equals(((Num)o).digits));
    }
  }

  public static class BigNum extends Num {
    public String toString() { return new String("BIGNUM("+digits+")"); }    
    public BigNum(String digits) { super(digits); }
  }

  // Oh, what I wouldn't give for Java5 varargs and autoboxing
  public static Long o(int l) { return new Long(l); }
  public static Long o(long l) { return new Long(l); }
  public static Double o(double d) { return new Double(d); }
  public static Boolean o(boolean b) { return new Boolean(b); }
  public static Num n(String digits) { return new Num(digits); }
  public static Num bn(String digits) { return new BigNum(digits); }
  public static Object t = new Boolean(true);
  public static Object f = new Boolean(false);
  public static Object a = new Object(){public String toString() {return "ARRAY_START";}};
  public static Object A = new Object(){public String toString() {return "ARRAY_END";}};
  public static Object m = new Object(){public String toString() {return "OBJECT_START";}};
  public static Object M = new Object(){public String toString() {return "OBJECT_END";}};
  public static Object N = new Object(){public String toString() {return "NULL";}};
  public static Object e = new Object(){public String toString() {return "EOF";}};

  // match parser states with the expected states
  public static void parse(JSONParser p, String input, Object[] expected) throws IOException {
    for (int i=0; i<expected.length; i++) {
      int ev = p.nextEvent();
      Object exp = expected[i];
      Object got = null;

      switch(ev) {
        case JSONParser.ARRAY_START: got=a; break;
        case JSONParser.ARRAY_END: got=A; break;
        case JSONParser.OBJECT_START: got=m; break;
        case JSONParser.OBJECT_END: got=M; break;
        case JSONParser.LONG: got=o(p.getLong()); break;
        case JSONParser.NUMBER:
          if (exp instanceof Double) {
            got = o(p.getDouble());
          } else {
            got = n(p.getNumberChars().toString());
          }
          break;
        case JSONParser.BIGNUMBER: got=bn(p.getNumberChars().toString()); break;
        case JSONParser.NULL: got=N; p.getNull(); break; // optional
        case JSONParser.BOOLEAN: got=o(p.getBoolean()); break;
        case JSONParser.EOF: got=e; break;
        case JSONParser.STRING: got=p.getString(); break;
        default: got="Unexpected Event Number " + ev;
      }

      if (!(exp==got || exp.equals(got))) {
        TestCase.fail("Fail: String='"+input+"'"
                + "\n\tINPUT=" + got
                + "\n\tEXPECTED=" + exp
                + "\n\tAT RULE " + i);
      }
    }
  }


  public static void parse(String input, Object[] expected) throws IOException {
    input = input.replace('\'','"');
    for (int i=0; i<Integer.MAX_VALUE; i++) {
      JSONParser p = getParser(input,i,-1);
      if (p==null) break;
      parse(p,input,expected);
    }
  }




  public static void err(String input) throws IOException {
    try {
      JSONParser p = getParser(input);
      while (p.nextEvent() != JSONParser.EOF);
    } catch (Exception e) {
      return;
    }
    TestCase.fail("Input should failed:'" + input + "'");    
  }

  public void testNull() throws IOException {
    err("[nullz]");
    parse("[null]","[0]");
    parse("{'hi':null}",new Object[]{m,"hi",N,M,e});
  }

  public void testBool() throws IOException {
    err("[True]");
    err("[False]");
    err("[TRUE]");
    err("[FALSE]");
    err("[truex]");
    err("[falsex]"); 

    parse("[false,true, false , true ]",new Object[]{a,f,t,f,t,A,e});
  }

  public void testString() throws IOException {
    // NOTE: single quotes are converted to double quotes by this
    // testsuite!
    err("[']");
    err("[',]");
    err("{'}");
    err("{',}");

    err("['\\u111']");
    err("['\\u11']");
    err("['\\u1']");
    err("['\\']");
    err("['\\ ']");
    err("['\\U1111']");


    parse("['']",new Object[]{a,"",A,e});
    parse("['\\\\']",new Object[]{a,"\\",A,e});
    parse("['X\\\\']",new Object[]{a,"X\\",A,e});
    parse("['\\\\X']",new Object[]{a,"\\X",A,e});
    parse("['\\'']",new Object[]{a,"\"",A,e});


    String esc="\\n\\r\\tX\\b\\f\\/\\\\X\\\"";
    String exp="\n\r\tX\b\f/\\X\"";
    parse("['" + esc + "']",new Object[]{a,exp,A,e});
    parse("['" + esc+esc+esc+esc+esc + "']",new Object[]{a,exp+exp+exp+exp+exp,A,e});

    esc="\\u004A";
    exp="\u004A";
    parse("['" + esc + "']",new Object[]{a,exp,A,e});

    esc="\\u0000\\u1111\\u2222\\u12AF\\u12BC\\u19DE";
    exp="\u0000\u1111\u2222\u12AF\u12BC\u19DE";
    parse("['" + esc + "']",new Object[]{a,exp,A,e});

  }

  public void testNumbers() throws IOException {
    err("[00]");
    err("[003]");
    err("[00.3]");
    err("[1e1.1]");
    err("[+1]");
    err("[NaN]");
    err("[Infinity]");
    err("[--1]");


    String lmin    = "-9223372036854775808";
    String lminNot = "-9223372036854775809";
    String lmax    = "9223372036854775807";
    String lmaxNot = "9223372036854775808";

    String bignum="12345678987654321357975312468642099775533112244668800152637485960987654321";

    parse("[0,1,-1,543,-876]", new Object[]{a,o(0),o(1),o(-1),o(543),o(-876),A,e});
    parse("[-0]",new Object[]{a,o(0),A,e});


    parse("["+lmin +"," + lmax+"]",
          new Object[]{a,o(Long.MIN_VALUE),o(Long.MAX_VALUE),A,e});

    parse("["+bignum+"]", new Object[]{a,bn(bignum),A,e});
    parse("["+"-"+bignum+"]", new Object[]{a,bn("-"+bignum),A,e});

    parse("["+lminNot+"]",new Object[]{a,bn(lminNot),A,e});
    parse("["+lmaxNot+"]",new Object[]{a,bn(lmaxNot),A,e});

    parse("["+lminNot + "," + lmaxNot + "]",
          new Object[]{a,bn(lminNot),bn(lmaxNot),A,e});

    // bignum many digits on either side of decimal
    String t = bignum + "." + bignum;
    parse("["+t+","+"-"+t+"]", new Object[]{a,bn(t),bn("-"+t),A,e});
    err(t+".1"); // extra decimal
    err("-"+t+".1");

    // bignum exponent w/o fraction
    t = "1" + "e+" + bignum;
    parse("["+t+","+"-"+t+"]", new Object[]{a,bn(t),bn("-"+t),A,e});
    t = "1" + "E+" + bignum;
    parse("["+t+","+"-"+t+"]", new Object[]{a,bn(t),bn("-"+t),A,e});
    t = "1" + "e" + bignum;
    parse("["+t+","+"-"+t+"]", new Object[]{a,bn(t),bn("-"+t),A,e});
    t = "1" + "E" + bignum;
    parse("["+t+","+"-"+t+"]", new Object[]{a,bn(t),bn("-"+t),A,e});
    t = "1" + "e-" + bignum;
    parse("["+t+","+"-"+t+"]", new Object[]{a,bn(t),bn("-"+t),A,e});
    t = "1" + "E-" + bignum;
    parse("["+t+","+"-"+t+"]", new Object[]{a,bn(t),bn("-"+t),A,e});

    t = bignum + "e+" + bignum;
    parse("["+t+","+"-"+t+"]", new Object[]{a,bn(t),bn("-"+t),A,e});
    t = bignum + "E-" + bignum;
    parse("["+t+","+"-"+t+"]", new Object[]{a,bn(t),bn("-"+t),A,e});
    t = bignum + "e" + bignum;
    parse("["+t+","+"-"+t+"]", new Object[]{a,bn(t),bn("-"+t),A,e});

    t = bignum + "." + bignum + "e" + bignum;
    parse("["+t+","+"-"+t+"]", new Object[]{a,bn(t),bn("-"+t),A,e});

    err("[1E]");
    err("[1E-]");
    err("[1E+]");
    err("[1E+.3]");
    err("[1E+0.3]");
    err("[1E+1e+3]");
    err("["+bignum+"e"+"]");
    err("["+bignum+"e-"+"]");
    err("["+bignum+"e+"+"]");
    err("["+bignum+"."+bignum+"."+bignum+"]");


    double[] vals = new double[] {0,0.1,1.1,
            Double.MAX_VALUE,
            Double.MIN_VALUE,
            2.2250738585072014E-308, /* Double.MIN_NORMAL */
    };
    for (int i=0; i<vals.length; i++) {
      double d = vals[i];
      parse("["+d+","+-d+"]", new Object[]{a,o(d),o(-d),A,e});      
    }

    // MIN_NORMAL has the max number of digits (23), so check that
    // adding an extra digit causes BIGNUM to be returned.
    t = "2.2250738585072014E-308" + "0";
    parse("["+t+","+"-"+t+"]", new Object[]{a,bn(t),bn("-"+t),A,e});
    // check it works with a leading zero too
    t = "0.2250738585072014E-308" + "0";
    parse("["+t+","+"-"+t+"]", new Object[]{a,bn(t),bn("-"+t),A,e});

    // check that overflow detection is working properly w/ numbers that don't cause a wrap to negatives
    // when multiplied by 10
    t = "1910151821265210155" + "0";
    parse("["+t+","+"-"+t+"]", new Object[]{a,bn(t),bn("-"+t),A,e});

    for (int i=0; i<1000000; i++) {
      long val = r.nextLong();
      String sval = Long.toString(val);
      JSONParser parser = getParser("["+val+"]");
      parser.nextEvent();
      assertTrue(parser.nextEvent() == JSONParser.LONG);
      if (r.nextBoolean()) {
        assertEquals(val, parser.getLong());
      } else {
        CharArr chars = parser.getNumberChars();
        assertEquals(sval, chars.toString());
      }
    }

  }

  public void testArray() throws IOException {
    parse("[]","[]");
    parse("[ ]","[]");
    parse(" \r\n\t[\r\t\n ]\r\n\t ","[]");

    parse("[0]","[l]");
    parse("['0']","[s]");
    parse("[0,'0',0.1]","[lsn]");

    parse("[[[[[]]]]]","[[[[[]]]]]");
    parse("[[[[[0]]]]]","[[[[[l]]]]]");

    err("]");
    err("[");
    err("[[]");
    err("[]]");
    err("[}");
    err("{]");
    err("['a':'b']");
  }

  public void testObject() throws IOException {
    parse("{}","{}");
    parse("{}","{}");
    parse(" \r\n\t{\r\t\n }\r\n\t ","{}");

    parse("{'':null}","{s0}");

    err("}");
    err("[}]");
    err("{");
    err("[{]");
    err("{{}");
    err("[{{}]");
    err("{}}");;
    err("[{}}]");;
    err("{1}");
    err("[{1}]");
    err("{'a'}");
    err("{'a','b'}");
    err("{null:'b'}");
    err("{[]:'b'}");
    err("{true:'b'}");
    err("{false:'b'}");
    err("{{'a':'b'}:'c'}");

    parse("{"+"}", new Object[]{m,M,e});
    parse("{'a':'b'}", new Object[]{m,"a","b",M,e});
    parse("{'a':5}", new Object[]{m,"a",o(5),M,e});
    parse("{'a':null}", new Object[]{m,"a",N,M,e});
    parse("{'a':[]}", new Object[]{m,"a",a,A,M,e});
    parse("{'a':{'b':'c'}}", new Object[]{m,"a",m,"b","c",M,M,e});

    String big = "Now is the time for all good men to come to the aid of their country!";
    String t = big+big+big+big+big;
    parse("{'"+t+"':'"+t+"','a':'b'}", new Object[]{m,t,t,"a","b",M,e});
  }



  public void testAPI() throws IOException {
    JSONParser p = new JSONParser("[1,2]");
    assertEquals(JSONParser.ARRAY_START, p.nextEvent());
    // no nextEvent for the next objects...
    assertEquals(1,p.getLong());
    assertEquals(2,p.getLong());
    

  }

}
