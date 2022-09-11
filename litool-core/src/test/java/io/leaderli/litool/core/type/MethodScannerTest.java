package io.leaderli.litool.core.type;

import io.leaderli.litool.core.meta.Lira;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * @author leaderli
 * @since 2022/7/26 8:47 AM
 */
class MethodScannerTest {

    private final String name = Lira.of().filter(f -> f).get().toString();


    @Test
    void test() throws InvocationTargetException, IllegalAccessException {


        MethodScanner methodScanner = new MethodScanner(MethodScannerTest.class, true, null);


        Lira<Method> scan = methodScanner.scan();
        Assertions.assertEquals("test", scan.first().get().getName());
        Assertions.assertTrue(scan.first(f -> f.getName().equals("toString")).absent());

        methodScanner.set_scan_lambda();
        Assertions.assertTrue(methodScanner.scan().filter(f -> f.getName().contains("$")).present());

        methodScanner = new MethodScanner(MethodScannerTest.class, true, null);
        methodScanner.set_scan_object();
        scan = methodScanner.scan();
        Assertions.assertTrue(scan.first(f -> f.getName().equals("toString")).present());

        methodScanner = new MethodScanner(MethodScannerTest.class, true, Method::isDefault);
        scan = methodScanner.scan();
        Assertions.assertTrue(scan.absent());


    }


}
