/*
 * Copyright 2014 Lukas Krejci
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License
 */

package org.revapi.java.model;

import javax.annotation.Nonnull;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.VariableElement;

import org.revapi.Archive;
import org.revapi.Element;
import org.revapi.java.compilation.ProbingEnvironment;
import org.revapi.java.spi.JavaMethodParameterElement;
import org.revapi.java.spi.Util;

/**
 * @author Lukas Krejci
 * @since 0.1
 */
public final class MethodParameterElement extends JavaElementBase<VariableElement> implements
    JavaMethodParameterElement {

    private final int index;
    private String comparableSignature;

    public MethodParameterElement(ProbingEnvironment env, Archive archive, VariableElement element) {
        super(env, archive, element);
        if (element.getEnclosingElement() instanceof ExecutableElement) {
            index = ((ExecutableElement) element.getEnclosingElement()).getParameters().indexOf(element);
        } else {
            throw new IllegalArgumentException(
                "MethodParameterElement cannot be constructed using a VariableElement not representing a method parameter.");
        }
    }

    @Nonnull
    @Override
    protected String getHumanReadableElementType() {
        return "method parameter";
    }

    @Override
    public int compareTo(@Nonnull Element o) {
        int ret = super.compareTo(o);

        if (ret == 0) {
            MethodParameterElement other = (MethodParameterElement) o;
            ret = index - other.index;
        }

        return ret;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }

        if (obj == this) {
            return true;
        }

        if (!(obj instanceof MethodParameterElement)) {
            return false;
        }

        MethodParameterElement other = (MethodParameterElement) obj;

        ExecutableElement myMethodElement = (ExecutableElement) getModelElement().getEnclosingElement();
        ExecutableElement otherMethodElement = (ExecutableElement) other.getModelElement().getEnclosingElement();

        if (myMethodElement.getParameters().size() != otherMethodElement.getParameters().size()) {
            return false;
        }

        String myType = Util.toUniqueString(myMethodElement.getEnclosingElement().asType());
        String myMethod = myMethodElement.getSimpleName().toString();

        String otherType = Util.toUniqueString(otherMethodElement.getEnclosingElement().asType());
        String otherMethod = otherMethodElement.getSimpleName().toString();


        return myType.equals(otherType) && myMethod.equals(otherMethod) && index == other.index;
    }

    @Override
    protected String createComparableSignature() {
        String myType = Util.toUniqueString(getModelElement().getEnclosingElement().getEnclosingElement().asType());
        String myMethod = getModelElement().getEnclosingElement().getSimpleName().toString();

        return myType + "::" + myMethod;
    }
}
