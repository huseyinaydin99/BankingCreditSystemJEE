package tr.com.huseyinaydin.web.controllers;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import tr.com.huseyinaydin.application.cqrs.Mediator;
import tr.com.huseyinaydin.application.customers.commands.CreateIndividualCustomerCommand;
import tr.com.huseyinaydin.application.customers.commands.DeleteIndividualCustomerCommand;
import tr.com.huseyinaydin.application.customers.commands.UpdateIndividualCustomerCommand;
import tr.com.huseyinaydin.application.customers.dtos.CreatedIndividualCustomerResponse;
import tr.com.huseyinaydin.application.customers.dtos.DeletedIndividualCustomerResponse;
import tr.com.huseyinaydin.application.customers.dtos.IndividualCustomerResponse;
import tr.com.huseyinaydin.application.customers.dtos.UpdatedIndividualCustomerResponse;
import tr.com.huseyinaydin.application.customers.queries.GetByIdIndividualCustomerQuery;
import tr.com.huseyinaydin.application.customers.queries.GetListIndividualCustomerQuery;
import tr.com.huseyinaydin.sharedkernel.pagination.Paginate;

import java.util.UUID;

@RestController
@RequestMapping("/api/individual-customers")
public class IndividualCustomersController extends BaseController {

    public IndividualCustomersController(Mediator mediator) {
        super(mediator);
    }

    @PostMapping
    public ResponseEntity<CreatedIndividualCustomerResponse> create(
            @RequestBody @Valid CreateIndividualCustomerCommand command) {
        return ResponseEntity.status(HttpStatus.CREATED).body(mediator.send(command));
    }

    @PutMapping("/{id}")
    public ResponseEntity<UpdatedIndividualCustomerResponse> update(
            @PathVariable UUID id,
            @RequestBody @Valid UpdateIndividualCustomerCommand body) {
        UpdateIndividualCustomerCommand cmd = new UpdateIndividualCustomerCommand(
                id, body.firstName(), body.lastName(), body.dateOfBirth(),
                body.motherName(), body.fatherName(),
                body.phoneNumber(), body.email(), body.address()
        );
        return ResponseEntity.ok(mediator.send(cmd));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<DeletedIndividualCustomerResponse> delete(@PathVariable UUID id) {
        return ResponseEntity.status(HttpStatus.NO_CONTENT)
                .body(mediator.send(new DeleteIndividualCustomerCommand(id)));
    }

    @GetMapping("/{id}")
    public ResponseEntity<IndividualCustomerResponse> getById(@PathVariable UUID id) {
        return ResponseEntity.ok(mediator.query(new GetByIdIndividualCustomerQuery(id)));
    }

    @GetMapping
    public ResponseEntity<Paginate<IndividualCustomerResponse>> getList(
            @RequestParam(defaultValue = "0") int pageIndex,
            @RequestParam(defaultValue = "10") int pageSize) {
        return ResponseEntity.ok(mediator.query(new GetListIndividualCustomerQuery(pageIndex, pageSize)));
    }
}
