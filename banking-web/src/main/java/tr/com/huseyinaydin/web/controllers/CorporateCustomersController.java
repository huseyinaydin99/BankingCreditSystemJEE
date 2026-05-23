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
import tr.com.huseyinaydin.application.customers.commands.CreateCorporateCustomerCommand;
import tr.com.huseyinaydin.application.customers.commands.DeleteCorporateCustomerCommand;
import tr.com.huseyinaydin.application.customers.commands.UpdateCorporateCustomerCommand;
import tr.com.huseyinaydin.application.customers.dtos.CorporateCustomerResponse;
import tr.com.huseyinaydin.application.customers.dtos.CreatedCorporateCustomerResponse;
import tr.com.huseyinaydin.application.customers.dtos.DeletedCorporateCustomerResponse;
import tr.com.huseyinaydin.application.customers.dtos.UpdatedCorporateCustomerResponse;
import tr.com.huseyinaydin.application.customers.queries.GetByIdCorporateCustomerQuery;
import tr.com.huseyinaydin.application.customers.queries.GetListCorporateCustomerQuery;
import tr.com.huseyinaydin.sharedkernel.pagination.Paginate;

import java.util.UUID;

@RestController
@RequestMapping("/api/corporate-customers")
public class CorporateCustomersController extends BaseController {

    public CorporateCustomersController(Mediator mediator) {
        super(mediator);
    }

    @PostMapping
    public ResponseEntity<CreatedCorporateCustomerResponse> create(
            @RequestBody @Valid CreateCorporateCustomerCommand command) {
        return ResponseEntity.status(HttpStatus.CREATED).body(mediator.send(command));
    }

    @PutMapping("/{id}")
    public ResponseEntity<UpdatedCorporateCustomerResponse> update(
            @PathVariable UUID id,
            @RequestBody @Valid UpdateCorporateCustomerCommand body) {
        UpdateCorporateCustomerCommand cmd = new UpdateCorporateCustomerCommand(
                id, body.companyName(), body.taxOffice(),
                body.companyRegistrationNumber(), body.authorizedPersonName(),
                body.companyFoundationDate(), body.phoneNumber(), body.email(), body.address()
        );
        return ResponseEntity.ok(mediator.send(cmd));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<DeletedCorporateCustomerResponse> delete(@PathVariable UUID id) {
        return ResponseEntity.status(HttpStatus.NO_CONTENT)
                .body(mediator.send(new DeleteCorporateCustomerCommand(id)));
    }

    @GetMapping("/{id}")
    public ResponseEntity<CorporateCustomerResponse> getById(@PathVariable UUID id) {
        return ResponseEntity.ok(mediator.query(new GetByIdCorporateCustomerQuery(id)));
    }

    @GetMapping
    public ResponseEntity<Paginate<CorporateCustomerResponse>> getList(
            @RequestParam(defaultValue = "0") int pageIndex,
            @RequestParam(defaultValue = "10") int pageSize) {
        return ResponseEntity.ok(mediator.query(new GetListCorporateCustomerQuery(pageIndex, pageSize)));
    }
}
