N o g g i t
-----------

Noggit is the world's fastest streaming JSON parser for Java.

Features:
 - Fast!  Measured as the fastest JSON parser on char[], String input.
 - Streaming API (StAX/pull-parser like) for both easy and efficient parsing
 - Conforms to the JSON standard: http://www.ietf.org/rfc/rfc4627.txt
 - Memory efficiency
    - incremental parsing (Reader-based) in order to handle huge messages
    - a single byte of state needed per nested object or array
    - does not read large objects (including primitives) completely into memory unless asked
    - can eliminate most copying, allowing the user to provide the output buffer for values
 - can handle primitives of any size (does not attempt to parse
   numerics into a certain language primitives unless asked)
 - Simple serialization of objects (List, Map, etc)
 - Optional creation of objects (List, Map, etc) when parsing.

