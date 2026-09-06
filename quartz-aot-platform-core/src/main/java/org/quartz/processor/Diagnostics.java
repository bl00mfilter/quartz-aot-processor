package org.quartz.processor;

import javax.annotation.processing.Messager;
import javax.lang.model.element.Element;
import javax.tools.Diagnostic;

public final class Diagnostics {

    private Messager messager;

    public void init(Messager messager) {
        this.messager = messager;
    }

    public void error(String message, Element element) {
        messager.printMessage(Diagnostic.Kind.ERROR, message, element);
    }

    public void error(String message) {
        messager.printMessage(Diagnostic.Kind.ERROR, message);
    }

    public void note(String message) {
        messager.printMessage(Diagnostic.Kind.NOTE, message);
    }
}
