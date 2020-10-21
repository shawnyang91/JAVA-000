package jvm;

import java.io.*;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class MyClassLoader extends ClassLoader {

    public static void main(String[] args) {
        // write your code here
        try {
            MyClassLoader myClassLoader = new MyClassLoader();
            Class<?> hello = myClassLoader.loadClass("Hello");
            System.out.println(hello.getName());
            Method helloMethod = hello.getMethod("hello");
            System.out.println(helloMethod.getName());
            helloMethod.invoke(hello.newInstance());
        } catch (ClassNotFoundException | NoSuchMethodException | IllegalAccessException | InstantiationException | InvocationTargetException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected Class<?> findClass(String name) {
        byte[] bytes = readFile("/Users/yangshumin/Documents/学习提升/java 训练营/workspace/week01/MyClassLoader/Hello.xlass");
        return defineClass(name, bytes, 0, bytes.length);
    }

    private byte[] readFile(String filePath) {
        File file = new File(filePath);
        try (ByteArrayOutputStream bos = new ByteArrayOutputStream();
             FileInputStream inputStream = new FileInputStream(file)) {
            byte[] bytes = new byte[2048];
            byte[] decodedBytes = new byte[2048];
            int len = 0;
            while ((len = inputStream.read(bytes)) != -1) {
                for (int i = 0; i < bytes.length; i++) {
                    decodedBytes[i] = (byte) (255 - bytes[i]);
                }
                bos.write(decodedBytes, 0, len);
            }
            return bos.toByteArray();
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }
}
