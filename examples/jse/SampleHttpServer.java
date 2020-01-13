import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import com.sun.net.httpserver.spi.HttpServerProvider;
import org.luaj.vm2.Globals;
import org.luaj.vm2.LuaValue;
import org.luaj.vm2.lib.jse.JsePlatform;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.InetSocketAddress;
import java.net.URI;

/**
 * Created by nirenr on 2019/11/8.
 */
public class SampleHttpServer {
    public static void main(String[] args) throws IOException {
        // create an environment to run in
        // Use the convenience function on Globals to load a chunk.
        HttpServerProvider provider = HttpServerProvider.provider();
        HttpServer httpserver = provider.createHttpServer(new InetSocketAddress(8080), 100);//监听端口8080,能同时接受100个请求
        httpserver.createContext("/", new LuaResponseHandler());
        httpserver.setExecutor(null);
        httpserver.start();
        System.out.println("server started");
    }

    public static class LuaResponseHandler implements HttpHandler {
        private Globals globals = JsePlatform.standardGlobals();

        public LuaResponseHandler() {
            String script = "examples/lua/http.lua";
            LuaValue chunk = globals.loadfile(script);
            chunk.call();
        }

        @Override
        public void handle(HttpExchange httpExchange) throws IOException {
           // System.out.println("receive");
            String requestMethod = httpExchange.getRequestMethod();
            //在这里通过httpExchange获取客户端发送过来的消息
            URI uri = httpExchange.getRequestURI();
            String path = uri.getPath().substring(1);
            LuaValue func = globals.get(path);
            if (path.isEmpty()) {
                func = globals.get("main");
                System.out.println("server path main");
            }
            System.out.println("server path " + uri.getPath());
            System.out.println("server method " + requestMethod);

            if (requestMethod.equalsIgnoreCase("GET")) {//客户端的请求是get方法
                //设置服务端响应的编码格式，否则在客户端收到的可能是乱码

                Headers responseHeaders = httpExchange.getResponseHeaders();
                responseHeaders.set("Content-Type", "text/html;charset=utf-8");

                String response = "404 not found";

                if (func.isfunction()) {
                    response = func.jcall(uri.getQuery()).toString();
                }
                System.out.println("server response " + response);

                httpExchange.sendResponseHeaders(HttpURLConnection.HTTP_OK, response.length());

                OutputStream responseBody = httpExchange.getResponseBody();
                OutputStreamWriter writer = new OutputStreamWriter(responseBody, "UTF-8");
                writer.write(response);
                writer.close();
                responseBody.close();
            }
            if (requestMethod.equalsIgnoreCase("POST")) {//客户端的请求是post方法
                //设置服务端响应的编码格式，否则在客户端收到的可能是乱码
                Headers responseHeaders = httpExchange.getResponseHeaders();
                responseHeaders.set("Content-Type", "text/html;charset=utf-8");

                //在这里通过httpExchange获取客户端发送过来的消息
                BufferedReader buff = new BufferedReader(new InputStreamReader(httpExchange.getRequestBody()));
                StringBuilder buf = new StringBuilder();
                String line;
                while ((line = buff.readLine()) != null) {
                    System.out.println("server post " + line);
                    buf.append(line).append("\n");
                }
                String response = "404 not found";

                if (func.isfunction()) {
                    response = func.call(buf.toString()).tojstring();
                }

                httpExchange.sendResponseHeaders(HttpURLConnection.HTTP_OK, response.getBytes("UTF-8").length);

                OutputStream responseBody = httpExchange.getResponseBody();
                OutputStreamWriter writer = new OutputStreamWriter(responseBody, "UTF-8");
                writer.write(response);
                writer.close();
                responseBody.close();
            }
        }
    }
}

