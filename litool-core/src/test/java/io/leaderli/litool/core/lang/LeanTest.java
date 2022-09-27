package io.leaderli.litool.core.lang;

import com.google.gson.Gson;
import io.leaderli.litool.core.lang.lean.Lean;
import io.leaderli.litool.core.lang.lean.LeanFieldAdapter;
import io.leaderli.litool.core.lang.lean.LeanKey;
import io.leaderli.litool.core.lang.lean.TypeAdapter;
import io.leaderli.litool.core.test.StringValues;
import io.leaderli.litool.core.type.LiTypeToken;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * @author leaderli
 * @since 2022/9/24 9:41 AM
 */
@SuppressWarnings("rawtypes")
class LeanTest {


    Gson gson = new Gson();

    @Test
    void test() {

        String json = "{\"name\":\"1\",\"bean\": {\"name\": \"2\"},\"beans\": [{\"name\": \"3\"}]}";
        Map map = gson.fromJson(json, Map.class);
        LiTypeToken<Bean1<Bean1>> parameterized = LiTypeToken.getParameterized(Bean1.class, Bean1.class);

        Lean lean = new Lean();
        Bean1<Bean1> parser = lean.fromBean(map, parameterized);
        Assertions.assertEquals("3", parser.beans.get(0).name);

    }

    @Test
    void test2() {


        Map map = gson.fromJson("{\"name\":\"1\",\"bean3\": {\"name\": \"2\",\"bean2\": {\"name\": \"3\"}}}", Map.class);
        Lean lean = new Lean();
        Bean2 parser = lean.fromBean(map, Bean2.class);

        Assertions.assertEquals("3", parser.bean3.bean2.name);
        Assertions.assertNotNull(parser);

    }

    @Test
    void test3() {
        String json = "{\"name\":\"1\",\"bean\": {\"name\": \"2\"},\"beans\": [{\"name\": \"3\"}]}";
        Map map = gson.fromJson(json, Map.class);
        LiTypeToken<Bean4<Bean4>> parameterized = LiTypeToken.getParameterized(Bean4.class, Bean4.class);

        Lean lean = new Lean();
        Bean4<Bean4> parser = lean.fromBean(map, parameterized);
        Assertions.assertEquals("3", parser.beans.get(0).name);

    }

    @Test
    void test5() {
        String json = "{\"bean1\": {\"name\": \"2\"},\"bean2\": {\"name\": \"3\"}}";
        Map map = gson.fromJson(json, Map.class);

        Lean lean = new Lean();
        Bean5 parser = lean.fromBean(map, Bean5.class);

        Assertions.assertSame(lean.getAdapter(Bean1.class), lean.getAdapter(Bean1.class));

    }

    @Test
    void tes6() {
        String json = "{\"age\": 1.0}";
        Map map = gson.fromJson(json, Map.class);

        Lean lean = new Lean();
        Bean6 parser = lean.fromBean(map, Bean6.class);

        Assertions.assertEquals(1.0, parser.age);


    }

    @Test
    void test7() {
        String json = "{\"age\": 1.0,\"age2\": 2.0}";
        Map map = gson.fromJson(json, Map.class);

        Lean lean = new Lean(new LinkedHashMap<>(), Collections.singletonList(f -> {
            if (f.getName().equals("custom")) {
                return "age";
            }
            return null;
        }));
        Bean7 parser = lean.fromBean(map, Bean7.class);

        Assertions.assertEquals(1.0, parser.fake);
        Assertions.assertEquals(2.0, parser.fake2);
        Assertions.assertEquals(1.0, parser.custom);

    }

    @Test
    void test8() {
        String json = "{\"name\": [\"123\"],\"age\": 10}";
        Map map = gson.fromJson(json, Map.class);
        Lean lean = new Lean();
        Assertions.assertThrows(IllegalArgumentException.class, () -> lean.fromBean(map, Bean8.class));

    }

    @Test
    void test9() {
        String json = "{\"name\": [\"123\"],\"age\": 10}";
        Map map = gson.fromJson(json, Map.class);
        Lean lean = new Lean();
        Bean9 bean = lean.fromBean(map, Bean9.class);
        Assertions.assertEquals("123", bean.name);
    }

    @Test
    void test10() {
        String json = "{\"name\": \"123\"}";
        Map map = gson.fromJson(json, Map.class);
        Lean lean = new Lean();
        Bean10 bean = lean.fromBean(map, Bean10.class);
        Assertions.assertEquals("123", bean.name);
    }

    @Test
    void test11() {
        String json = "{\"name\": [\"123\"],\"age\": [10,18]}";
        Map map = gson.fromJson(json, Map.class);
        Lean lean = new Lean();
        Bean11<Integer> bean = lean.fromBean(map, LiTypeToken.getParameterized(Bean11.class, Integer.class));
        Assertions.assertArrayEquals(new String[]{"123"}, bean.name);
        //TODO fix
        Assertions.assertArrayEquals(new Integer[]{10, 18}, bean.ages);
    }

    private static class Bean11<T> {
        private String[] name;
        private T[] ages;
    }

    private static class Bean10 {
        private String name;
    }

    private static class StringTypeAdapter implements TypeAdapter<String> {

        @Override
        public String read(Object source) {
            if (source instanceof List) {
                return (String) ((List<?>) source).get(0);
            }
            return source + "";
        }
    }

    private static class Bean9 {
        @LeanFieldAdapter(StringTypeAdapter.class)
        private String name;
    }

    private static class Bean8 {
        @LeanFieldAdapter(StringTypeAdapter.class)
        private String name;
        @LeanFieldAdapter(StringTypeAdapter.class)
        private int age;
    }

    private static class Bean7 {
        @LeanKey("age")
        private double fake;
        @LeanKey("age2")
        private double fake2;
        @StringValues({"age3", "age2"})
        private double custom;
    }

    private static class Bean6 {
        private double age;
    }

    private static class Bean5 {
        private Bean1 bean1;
        private Bean1 bean2;

    }

    private static class Bean4<T> {
        private String name;
        private Map bean;
        private List<T> beans;
    }

    private static class Bean1<T> {
        private String name;
        private Bean1 bean;
        private List<T> beans;
    }

    private static class Bean2 {
        private Bean3 bean3;
        private String name;

    }

    private static class Bean3 {
        private String name;
        private Bean2 bean2;
    }
}
