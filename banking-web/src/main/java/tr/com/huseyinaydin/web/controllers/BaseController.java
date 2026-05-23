package tr.com.huseyinaydin.web.controllers;

import tr.com.huseyinaydin.application.cqrs.Mediator;

public abstract class BaseController {

    protected final Mediator mediator;

    protected BaseController(Mediator mediator) {
        this.mediator = mediator;
    }
}
