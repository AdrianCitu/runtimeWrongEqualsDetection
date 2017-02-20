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

import com.sun.source.tree.*;
import com.sun.source.util.*;
import com.sun.tools.javac.tree.JCTree;

import javax.lang.model.element.Name;
import javax.lang.model.type.PrimitiveType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;
import java.util.List;


/**
 * Tree visitor that will override the method execution; this implementation
 * is using heavily the cast of internal classes not the interfaces.
 */
public class CodePatternTreeVisitor extends TreePathScanner<Void, Void> {

    // offsets of AST nodes in source file
    private final SourcePositions sourcePositions;
    // bridges Compiler api, Annotation Processing API and Tree API
    private final Trees trees;
    // utility to operate on types
    private final Types types;
    private final PrimitiveType booleanType;
    private final Name getName;
    private final Name equalsName;



    private CompilationUnitTree currCompUnit;

    public CodePatternTreeVisitor(JavacTask task) {
        types = task.getTypes();
        trees = Trees.instance(task);
        sourcePositions = trees.getSourcePositions();

// utility to operate on program elements
        Elements elements = task.getElements();
// create a Name object representing the method name to match against
        getName = elements.getName("get");


        equalsName = elements.getName("equals");

        booleanType = types.getPrimitiveType(TypeKind.BOOLEAN);

    }

    @Override
    public Void visitCompilationUnit(CompilationUnitTree tree, Void p) {
        currCompUnit = tree;
        return super.visitCompilationUnit(tree, p);
    }


    @Override
    public Void visitMethodInvocation(MethodInvocationTree methodInvocationTree, Void aVoid) {
        ExpressionTree methodSelect = methodInvocationTree.getMethodSelect();
        if (isEqualsCall(new TreePath(getCurrentPath(), methodSelect))) {
            MemberSelectTree jcmi = (MemberSelectTree) methodSelect;
            ExpressionTree jcmiExpression = jcmi.getExpression();

            com.sun.tools.javac.util.Name flatName = getFlatName(jcmiExpression);

            if (flatName != null) {
                List<? extends ExpressionTree> arguments = methodInvocationTree.getArguments();
                if (arguments != null && arguments.size() == 1) {
                    ExpressionTree tree = arguments.get(0);
                    com.sun.tools.javac.util.Name otherFlatName = getFlatName(tree);
                    if (otherFlatName != null && !flatName.equals(otherFlatName)) {
                        System.err.println("Try to call equals on different parameters at line "
                                + getLineNumber(methodInvocationTree)
                                + " of file " +
                                currCompUnit.getSourceFile().getName()
                                + "; this is a bug!"
                        );

                    }
                }
            }
        }

        return super.visitMethodInvocation(methodInvocationTree, aVoid);
    }

    private com.sun.tools.javac.util.Name getFlatName(ExpressionTree expressionTree) {
        if (expressionTree instanceof JCTree.JCIdent) {
            JCTree.JCIdent ident = (JCTree.JCIdent) expressionTree;
            return ident.type.tsym.flatName();
        }
        return null;
    }


    private boolean isEqualsCall(TreePath path) {

        Tree pathLeaf = path.getLeaf();
        if (pathLeaf instanceof JCTree.JCFieldAccess) {
            JCTree.JCFieldAccess leaf = (JCTree.JCFieldAccess) pathLeaf;
            return leaf.name == equalsName && leaf.type.getReturnType() == booleanType;
        }
        return false;
    }

    private long getLineNumber(Tree tree) {
        // map offsets to line numbers in source file
        LineMap lineMap = currCompUnit.getLineMap();
        if (lineMap == null)
            return -1;
        // find offset of the specified AST node
        long position = sourcePositions.getStartPosition(currCompUnit, tree);
        return lineMap.getLineNumber(position);
    }
}
