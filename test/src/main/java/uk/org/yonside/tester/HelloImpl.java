package uk.org.yonside.tester;

import uk.org.yonside.annotato.Service;

@Service
public final class HelloImpl implements HelloService {


    @Override
    public void sayHello() {
        System.out.println("Hello World");
    }
}
