# JavaPythonBridge
This is a simple demo which does remote function calls of simple functions from Python to Java.

The ServerBridge.java exposes a TCP server which ClientBridge.py can connect to in order to send function names and parameters and receive back results.

The communication protocol is UTF-8 encoded string based, with newlines (single LF byte) as terminators. Example function calls:
````c
ClientBridge.py: "acos;0.0\n"
ServerBridge.java: "1.5707963267948966\n"
ClientBridge.py: "sort;1;4;5;4;3;2;1\n"
ServerBridge.java: "5;2;3;4;1\n"
ClientBridge.py: "isSorted;1;2;4;8;9\n"
ServerBridge.java: "true\n"
````

## Library functions
This library contains Guava (31.1), Apache Commons Lang (3.12.0) and Apache Commons Math (3.6.1) methods, collected into the GuavaClass, LangClass and MathClass classes respectively.

See GUAVA_COPYING file for Guava license.
https://github.com/google/guava

See LANG_LICENSE.txt file for Apache Commons Lang license.
http://commons.apache.org/proper/commons-lang/

See MATH_LICENSE.txt file for Apache Commons Math license.
http://commons.apache.org/proper/commons-math/
