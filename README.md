N o g g i t
===========

Noggit is the world's fastest streaming JSON parser for Java.

Features:
---------
 - Fast!  Measured as the fastest JSON parser on char[], String input.
 - Streaming API (StAX/pull-parser like) for both easy and efficient parsing.
 - Conforms to JSON standard: http://www.ietf.org/rfc/rfc4627.txt
 - Conforms to JSON standard: http://rfc7159.net/rfc7159
 - Memory efficiency:
    - Incremental parsing (Reader-based) in order to handle huge messages.
    - A single byte of state needed per nested object or array.
    - Doesn't read large objects (including primitives) into memory unless asked.
    - Can eliminate most copying, allowing user to provide value output buffers.
 - Can handle primitives of any size (does not attempt to parse
   numerics into a certain language primitive unless asked).
 - Simple serialization of objects (List, Map, etc).
 - Optional creation of objects (List, Map, etc) when parsing.

Syntax Features (Optional):
---------------------------
 - Single-line comments using either # or //
 - Multi-line comments using C style /* comments in here */
 - Single quoted strings.
 - Unquoted object keys.  Example: {answer : 42}
 - Unquoted string values.  Example: {first: Yonik, last: Seeley}
 - Allow backslash escaping of any character.
 - Allow trailing commas and extra commas.  Example: [9,4,3,]
 - Handle nbsp (non-break space, \u00a0) as whitespace.
 - Optional opening and closing brackets. "a" :"Val1", "b" : "Val2" is equivalent to {"a" :"Val1", "b" : "Val2" }
 - Optional comma between objects {"a" :{"b:"c"} "d": {"e":"f"}} or [{"a":"b"}{"c":"d"}] are all valid
 - Optional colon between key and value {"a" {"c":"d"}} and {"a" ["b","c"]} are valid
