package uk.org.yonside.tester;

import java.util.ServiceLoader;

public class Main {

    public static void main(String[] args) {
        ServiceLoader<HelloService> loader = ServiceLoader.load(HelloService.class);
        HelloService svc = loader.findFirst().get();

        svc.sayHello();
    }
}
