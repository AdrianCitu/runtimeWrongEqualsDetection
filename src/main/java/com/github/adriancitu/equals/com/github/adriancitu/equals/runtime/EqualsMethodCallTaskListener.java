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
package com.github.adriancitu.equals.com.github.adriancitu.equals.runtime;

import com.sun.source.tree.CompilationUnitTree;
import com.sun.source.util.JavacTask;
import com.sun.source.util.TaskEvent;
import com.sun.source.util.TaskListener;

/**
 * Created by ady on 03.02.17.
 */
public class EqualsMethodCallTaskListener implements TaskListener{

    private final CodePatternTreeVisitor2 visitor;

    public EqualsMethodCallTaskListener(JavacTask javacTask) {

        visitor = new CodePatternTreeVisitor2(javacTask);
    }


    public void started(TaskEvent taskEvent) {

    }

    public void finished(TaskEvent taskEvent) {
        if(taskEvent.getKind() == TaskEvent.Kind.ANALYZE) {
            CompilationUnitTree compilationUnit = taskEvent.getCompilationUnit();
            visitor.scan(compilationUnit, null);
        }
    }

}
