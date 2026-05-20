package tr.com.huseyinaydin.web.controllers;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import tr.com.huseyinaydin.application.credittype.commands.CreateCreditTypeCommand;
import tr.com.huseyinaydin.application.credittype.dtos.CreditTypeResponse;
import tr.com.huseyinaydin.application.credittype.queries.GetListCreditTypeQuery;
import tr.com.huseyinaydin.application.cqrs.Mediator;
import tr.com.huseyinaydin.domain.enums.CustomerType;
import tr.com.huseyinaydin.sharedkernel.pagination.Paginate;

@RestController
@RequestMapping("/api/credit-types")
public class CreditTypesController {

    private final Mediator mediator;

    public CreditTypesController(Mediator mediator) {
        this.mediator = mediator;
    }

    @PostMapping
    public ResponseEntity<CreateCreditTypeCommand.Response> create(
            @RequestBody @Valid CreateCreditTypeCommand command) {
        return ResponseEntity.status(HttpStatus.CREATED).body(mediator.send(command));
    }

    @GetMapping
    public ResponseEntity<Paginate<CreditTypeResponse>> getList(
            @RequestParam(required = false) CustomerType customerType,
            @RequestParam(defaultValue = "0") int pageIndex,
            @RequestParam(defaultValue = "10") int pageSize) {
        return ResponseEntity.ok(mediator.query(
                new GetListCreditTypeQuery(customerType, pageIndex, pageSize)));
    }
}
