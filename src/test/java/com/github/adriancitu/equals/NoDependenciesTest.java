/**
 * MIT License
 *
 * Copyright (c) 2017 Adrian CITU
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 *
 */
package com.github.adriancitu.equals;

/**
 * Created by ady on 06.02.17.
 */
public class NoDependenciesTest {
    public static class TestClass1 {

        private String field;

        public TestClass1(String f) {
            this.field = f;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof TestClass1)) return false;

            TestClass1 that = (TestClass1) o;

            return field != null ? field.equals(that.field) : that.field == null;
        }

        @Override
        public int hashCode() {
            return field != null ? field.hashCode() : 0;
        }
    }

    public static class TestClass2 {
        private String field;

        public TestClass2(String f) {
            this.field = f;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof TestClass2)) return false;

            TestClass2 that = (TestClass2) o;

            return field.equals(that.field);
        }

        public String equals(TestClass1 t1) {
            return "";
        }


        @Override
        public int hashCode() {
            return field.hashCode();
        }
    }

    public static void main (String args) {

        NoDependenciesTest thisInstance = new NoDependenciesTest();

        TestClass1 t1 = new TestClass1("1");
        TestClass2 t2 = new TestClass2("2");


        System.out.println(t1.equals(t2));
        System.out.println(t1.equals(t1));
        System.out.println(t2.equals(t1));

        "titi".equals(new Boolean(true));
        new TestClass1("1").equals(new TestClass2("2"));
    }
}
