package library;

import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Arrays;

public class ServerBridge {
    public static final int DEFAULT_PORT = 1337;

    public static final byte[] MESSAGE_END = new byte[] { (byte) '\n' };

    public static final String ON_ERROR_BOOL = "false";
    public static final String ON_ERROR_INT = "0";
    public static final String ON_ERROR_FLOAT = "0.0";
    public static final String ON_ERROR_LIST = "";

    private final int portNumber;
    private ServerSocket serverSocket;
    private Socket clientSocket;
    private InputStream in;
    private OutputStream out;

    public ServerBridge(int portNumber) throws Exception {
        this.portNumber = portNumber;
        System.out.println("[ServerBridge] Starting at port: " + this.portNumber);
        this.serverSocket = new ServerSocket(this.portNumber);
    }

    public void loop() {
        while(true) {
            try {
                this.connect();
                this.clientLoop();
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                this.disconnect();
            }
        }
    }

    public void connect() throws Exception {
        System.out.println("[ServerBridge] Accepting connections at port: " + this.portNumber);
        this.clientSocket = serverSocket.accept();
        System.out.println("[ServerBridge] Connected from: " + this.clientSocket.getRemoteSocketAddress());
        this.in = clientSocket.getInputStream();
        this.out = clientSocket.getOutputStream();
    }

    public void disconnect() {
        System.out.println("[ServerBridge] Disconnected");
        try {
            this.in.close();
        } catch (Exception ignored){}
        try {
            this.out.close();
        } catch (Exception ignored){}
        try {
            this.clientSocket.close();
        } catch (Exception ignored){}
    }

    public void terminate() {
        System.out.println("[ServerBridge] Terminated");
        try {
            this.serverSocket.close();
        } catch (Exception ignored){}
    }

    public String nextMessage() throws Exception {
        // Read message bytes
        byte[] messageBytes = new byte[0];
        byte[] buffer = new byte[512];
        int read = 0;
        while (true) {
            read = this.in.read(buffer);
            if (read < 0) {
                return "";
            }
            messageBytes = concat(messageBytes, messageBytes.length, buffer, read);
            if (endsWith(messageBytes, MESSAGE_END)) {
                break;
            }
        }
        // Decode UTF-8 and return
        return new String(messageBytes, "UTF-8").strip(); 
    }

    public void sendResponse(String response) throws Exception {
        byte[] responseBytes = response.getBytes("UTF-8");
        this.out.write(responseBytes);
        this.out.write(MESSAGE_END);
    }

    public void clientLoop() throws Exception {
        while (true) {
            String message = this.nextMessage();
            if (message.isEmpty()) {
                return;
            }
            //System.out.println("[ServerBridge] Message: " + message);
            String response = processMessage(message);
            //System.out.println("[ServerBridge] Response: " + response);
            this.sendResponse(response);
        }
    }

    public static byte[] concat(byte[] a, int aLength, byte[] b, int bLength) {
        byte[] result = Arrays.copyOf(a, aLength + bLength);
        for (int i = 0 ; i < bLength ; i++) {
            result[aLength + i] = b[i];
        }
        return result;
    }

    public static boolean endsWith(byte[] a, byte[] b) {
        if (a.length < b.length) {
            return false;
        }
        return Arrays.equals(a, a.length - b.length, a.length, b, 0, b.length);
    }

    public static int[] parseIntArray(String[] parts, int begin, int length) {
        int[] result = new int[length];
        for (int i = 0 ; i < length ; i++) {
            result[i] = Integer.parseInt(parts[i + begin]);
        }
        return result;
    }

    public static byte[] parseByteArray(String[] parts, int begin, int length) {
        byte[] result = new byte[length];
        for (int i = 0 ; i < length ; i++) {
            result[i] = (byte) Integer.parseInt(parts[i + begin]);
        }
        return result;
    }

    public static boolean[] parseBoolArray(String[] parts, int begin, int length) {
        boolean[] result = new boolean[length];
        for (int i = 0 ; i < length ; i++) {
            result[i] = Boolean.parseBoolean(parts[i + begin]);
        }
        return result;
    }

    public static String processMessage(String message) {
        String[] parts = message.split(";");
        switch (parts[0]) {
            case "nextPrime":
            try {
                return String.valueOf(MathClass.nextPrime(Integer.parseInt(parts[1])));
            } catch (Exception ignore) {
                return ON_ERROR_INT;
            }
            case "isPrime":
            try {
                return String.valueOf(MathClass.isPrime(Integer.parseInt(parts[1])));
            } catch (Exception ignore) {
                return ON_ERROR_INT;
            }
            case "gcd":
            try {
                return String.valueOf(MathClass.gcd(Integer.parseInt(parts[1]), Integer.parseInt(parts[2])));
            } catch (Exception ignore) {
                return ON_ERROR_INT;
            }
            case "pow":
            try {
                return String.valueOf(MathClass.pow(Integer.parseInt(parts[1]), Integer.parseInt(parts[2])));
            } catch (Exception ignore) {
                return ON_ERROR_INT;
            }
            case "stirling":
            try {
                return String.valueOf(MathClass.stirlingS2(Integer.parseInt(parts[1]), Integer.parseInt(parts[2])));
            } catch (Exception ignore) {
                return ON_ERROR_INT;
            }
            case "acos":
            try {
                return String.valueOf(MathClass.acos(Double.parseDouble(parts[1])));
            } catch (Exception ignore) {
                return ON_ERROR_FLOAT;
            }
            case "log10":
            try {
                return String.valueOf(MathClass.log10(Double.parseDouble(parts[1])));
            } catch (Exception ignore) {
                return ON_ERROR_FLOAT;
            }
            case "sin":
            try {
                return String.valueOf(MathClass.sin(Double.parseDouble(parts[1])));
            } catch (Exception ignore) {
                return ON_ERROR_FLOAT;
            }
            case "sinh":
            try {
                return String.valueOf(MathClass.sinh(Double.parseDouble(parts[1])));
            } catch (Exception ignore) {
                return ON_ERROR_FLOAT;
            }
            case "tan":
            try {
                return String.valueOf(MathClass.tan(Double.parseDouble(parts[1])));
            } catch (Exception ignore) {
                return ON_ERROR_FLOAT;
            }
            case "isSorted":
            try {
                return String.valueOf(LangClass.isSorted(parseIntArray(parts, 1, parts.length - 1)));
            } catch (Exception ignore) {
                return ON_ERROR_BOOL;
            }
            case "indexOf":
            try {
                int sizearray = Integer.parseInt(parts[1]);
                return String.valueOf(GuavaClass.indexOf(parseBoolArray(parts, 2, sizearray), parseBoolArray(parts, 2 + sizearray, parts.length - 2 - sizearray)));
            } catch (Exception ignore) {
                return ON_ERROR_INT;
            }
            case "meanOf":
            try {
                return String.valueOf(GuavaClass.meanOf(parseIntArray(parts, 1, parts.length - 1)));
            } catch (Exception ignore) {
                return ON_ERROR_FLOAT;
            }
            case "min":
            try {
                return String.valueOf(GuavaClass.min(parseIntArray(parts, 1, parts.length - 1)));
            } catch (Exception ignore) {
                return ON_ERROR_INT;
            }
            case "sort":
            try {
                byte[] toSort = parseByteArray(parts, 3, parts.length - 3);
                if (toSort.length < 1) {
                    return "";
                }
                GuavaClass.sort(toSort, Integer.parseInt(parts[1]), Integer.parseInt(parts[2]));
                StringBuilder sb = new StringBuilder();
                for (byte b : toSort) {
                    sb.append(String.valueOf(b));
                    sb.append(";");
                }
                return sb.substring(0, sb.length() - 1);
            } catch (Exception ignore) {
                return ON_ERROR_LIST;
            }
        }
        throw new RuntimeException("Unknown function: " + parts[0]);
    }

    public static void main(String[] args) {
        ServerBridge server = null;
        int port = DEFAULT_PORT;

        if (args.length > 1) {
            port = Integer.parseInt(args[1]);
        }

        try {
            server = new ServerBridge(port);
            server.loop();
        } catch (Exception ex) {
            ex.printStackTrace();
        } finally {
            server.terminate();
        }
    }

}
