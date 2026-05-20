package tr.com.huseyinaydin.web.controllers;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import tr.com.huseyinaydin.application.creditapplication.commands.CreateCreditApplicationCommand;
import tr.com.huseyinaydin.application.creditapplication.dtos.CreditApplicationResponse;
import tr.com.huseyinaydin.application.creditapplication.queries.GetListByCustomerCreditApplicationQuery;
import tr.com.huseyinaydin.application.cqrs.Mediator;
import tr.com.huseyinaydin.sharedkernel.pagination.Paginate;

import java.util.UUID;

@RestController
@RequestMapping("/api/credit-applications")
public class CreditApplicationsController {

    private final Mediator mediator;

    public CreditApplicationsController(Mediator mediator) {
        this.mediator = mediator;
    }

    @PostMapping
    public ResponseEntity<CreateCreditApplicationCommand.Response> create(
            @RequestBody @Valid CreateCreditApplicationCommand command) {
        return ResponseEntity.status(HttpStatus.CREATED).body(mediator.send(command));
    }

    @GetMapping("/customer/{customerId}")
    public ResponseEntity<Paginate<CreditApplicationResponse>> getByCustomer(
            @PathVariable UUID customerId,
            @RequestParam(defaultValue = "0") int pageIndex,
            @RequestParam(defaultValue = "10") int pageSize) {
        return ResponseEntity.ok(mediator.query(
                new GetListByCustomerCreditApplicationQuery(customerId, pageIndex, pageSize)));
    }
}
