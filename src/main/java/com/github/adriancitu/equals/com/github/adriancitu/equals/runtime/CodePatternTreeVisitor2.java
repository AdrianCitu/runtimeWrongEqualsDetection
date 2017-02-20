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
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;
import java.util.List;
import java.util.Optional;

/**
 * Tree visitor that override the method invocation in order to check
 * that the invocated method is java.lang.Object.equals() and
 * the caller and the callee have the same type.
 */
public class CodePatternTreeVisitor2 extends TreePathScanner<Void, Void> {

    private final Types types;
    private final Trees trees;
    private final SourcePositions sourcePositions;
    private final Name equalsName;
    private final PrimitiveType booleanType;
    private CompilationUnitTree currCompUnit;

    public CodePatternTreeVisitor2(JavacTask task) {
        types = task.getTypes();
        trees = Trees.instance(task);
        sourcePositions = trees.getSourcePositions();

        Elements elements = task.getElements();

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


        final List<? extends ExpressionTree> arguments = methodInvocationTree.getArguments();
        final ExpressionTree methodSelect = methodInvocationTree.getMethodSelect();

        switch (methodInvocationTree.getKind()) {
            case METHOD_INVOCATION:
                Tree.Kind methodSelectKind = methodSelect.getKind();

                switch (methodSelect.getKind()) {
                    case MEMBER_SELECT:
                        //t1.equals
                        //or
                        //field.equals
                        MemberSelectTree memberSelectTree = (MemberSelectTree) methodSelect;

                        //it's a equals method invocation
                        if (isEqualsCall(
                                new TreePath(getCurrentPath(), methodSelect),
                                methodInvocationTree.getArguments() != null ?
                                        methodInvocationTree.getArguments().size() : 0)) {

                            //t1
                            //or
                            //field
                            ExpressionTree expression = memberSelectTree.getExpression();

                            TypeMirror callerType =
                                    trees.getTypeMirror(new TreePath(getCurrentPath(), expression));

                            Optional<TypeMirror> argumentType =
                                    computeArgumentType(methodInvocationTree.getArguments());

                            if (argumentType.isPresent() && !callerType.equals(argumentType.get())) {
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

    /*
     * The safest way to check if the call is a equals call was to cast the TreePath
     * interface to the real implementation; not very proud of me.
     */
    private boolean isEqualsCall(TreePath path, int methodParametersSize) {

        if (methodParametersSize != 1) {
            return false;
        }

        Tree pathLeaf = path.getLeaf();
        if (pathLeaf instanceof JCTree.JCFieldAccess) {
            JCTree.JCFieldAccess leaf = (JCTree.JCFieldAccess) pathLeaf;
            boolean returnValue = leaf.name == equalsName && leaf.type.getReturnType() == booleanType;

            return returnValue;
        }
        return false;
    }

    private Optional<TypeMirror> computeArgumentType(List<? extends ExpressionTree> arguments) {

        if (arguments == null || arguments.isEmpty() || arguments.size() != 1) {
            return Optional.empty();
        }

        final ExpressionTree expressionTree = arguments.get(0);


        switch (expressionTree.getKind()) {
            case IDENTIFIER:
            case NEW_CLASS:
                TypeMirror parameterType =
                        trees.getTypeMirror(new TreePath(getCurrentPath(), expressionTree));

                return Optional.of(parameterType);
        }
        return Optional.empty();
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
