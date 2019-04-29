package cn.com.module.rpc.service.proxy.impl;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.lang.reflect.Method;
import java.net.Socket;
import java.util.Map;

/**
 * 服务器任务
 *
 * @author YanZhen
 * @since 2019-04-27 15:48
 */
public class ServerTask implements Runnable {

  private Socket socket;
  private Map<String, Class> mapRegistry;

  public ServerTask(Socket socket, Map<String, Class> mapRegistry) {
    this.socket = socket;
    this.mapRegistry = mapRegistry;
  }

  @Override
  public void run() {
    try (
            final ObjectInputStream inputStream = new ObjectInputStream(socket.getInputStream());
            final ObjectOutputStream outputStream = new ObjectOutputStream(socket.getOutputStream())
    ) {
      final String name = inputStream.readUTF();
      final String method = inputStream.readUTF();
      final Class<?>[] classes = (Class<?>[]) inputStream.readObject();
      final Object[] os = (Object[]) inputStream.readObject();

      final Class aClass = mapRegistry.get(name);
      if (aClass == null) {
        throw new ClassNotFoundException();
      }

      final Method method1 = aClass.getMethod(method, classes);
      final Object invoke = method1.invoke(aClass.newInstance(), os);

      outputStream.writeObject(invoke);
    } catch (Exception e) {
      e.printStackTrace();
    } finally {
      if (socket != null) {
        try {
          socket.close();
        } catch (IOException e) {
          e.printStackTrace();
        }
      }
    }
  }
}
