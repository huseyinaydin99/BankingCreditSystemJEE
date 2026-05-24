package tr.com.huseyinaydin.web.controllers;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
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

@Tag(name = "Bireysel Müşteriler", description = "Bireysel müşteri oluşturma, güncelleme, silme ve listeleme işlemleri")
@SecurityRequirement(name = "bearerAuth")
@RestController
@RequestMapping("/api/individual-customers")
public class IndividualCustomersController extends BaseController {

    public IndividualCustomersController(Mediator mediator) {
        super(mediator);
    }

    @Operation(summary = "Bireysel müşteri oluştur",
               description = "Yeni bir bireysel müşteri kaydı oluşturur. TC Kimlik Numarası benzersiz olmalıdır.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Müşteri başarıyla oluşturuldu"),
            @ApiResponse(responseCode = "400", description = "Doğrulama hatası",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(ref = "#/components/schemas/ValidationErrorResponse"))),
            @ApiResponse(responseCode = "401", description = "Kimlik doğrulama gerekli",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(ref = "#/components/schemas/ErrorResponse"))),
            @ApiResponse(responseCode = "409", description = "TC Kimlik Numarası zaten kayıtlı",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(ref = "#/components/schemas/ErrorResponse")))
    })
    @PostMapping
    public ResponseEntity<CreatedIndividualCustomerResponse> create(
            @RequestBody @Valid CreateIndividualCustomerCommand command) {
        return ResponseEntity.status(HttpStatus.CREATED).body(mediator.send(command));
    }

    @Operation(summary = "Bireysel müşteri güncelle",
               description = "Mevcut bireysel müşterinin iletişim ve kimlik bilgilerini günceller.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Müşteri başarıyla güncellendi"),
            @ApiResponse(responseCode = "400", description = "Doğrulama hatası",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(ref = "#/components/schemas/ValidationErrorResponse"))),
            @ApiResponse(responseCode = "401", description = "Kimlik doğrulama gerekli",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(ref = "#/components/schemas/ErrorResponse"))),
            @ApiResponse(responseCode = "404", description = "Müşteri bulunamadı",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(ref = "#/components/schemas/ErrorResponse")))
    })
    @PutMapping("/{id}")
    public ResponseEntity<UpdatedIndividualCustomerResponse> update(
            @Parameter(description = "Güncellenecek müşterinin UUID'si", required = true)
            @PathVariable UUID id,
            @RequestBody @Valid UpdateIndividualCustomerCommand body) {
        UpdateIndividualCustomerCommand cmd = new UpdateIndividualCustomerCommand(
                id, body.firstName(), body.lastName(), body.dateOfBirth(),
                body.motherName(), body.fatherName(),
                body.phoneNumber(), body.email(), body.address()
        );
        return ResponseEntity.ok(mediator.send(cmd));
    }

    @Operation(summary = "Bireysel müşteri sil",
               description = "Belirtilen bireysel müşteriyi soft-delete ile pasifleştirir.")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Müşteri başarıyla silindi"),
            @ApiResponse(responseCode = "401", description = "Kimlik doğrulama gerekli",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(ref = "#/components/schemas/ErrorResponse"))),
            @ApiResponse(responseCode = "404", description = "Müşteri bulunamadı",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(ref = "#/components/schemas/ErrorResponse")))
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<DeletedIndividualCustomerResponse> delete(
            @Parameter(description = "Silinecek müşterinin UUID'si", required = true)
            @PathVariable UUID id) {
        return ResponseEntity.status(HttpStatus.NO_CONTENT)
                .body(mediator.send(new DeleteIndividualCustomerCommand(id)));
    }

    @Operation(summary = "ID ile bireysel müşteri getir",
               description = "UUID ile tekil bireysel müşteri kaydını döner.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Müşteri bulundu"),
            @ApiResponse(responseCode = "401", description = "Kimlik doğrulama gerekli",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(ref = "#/components/schemas/ErrorResponse"))),
            @ApiResponse(responseCode = "404", description = "Müşteri bulunamadı",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(ref = "#/components/schemas/ErrorResponse")))
    })
    @GetMapping("/{id}")
    public ResponseEntity<IndividualCustomerResponse> getById(
            @Parameter(description = "Getirilecek müşterinin UUID'si", required = true)
            @PathVariable UUID id) {
        return ResponseEntity.ok(mediator.query(new GetByIdIndividualCustomerQuery(id)));
    }

    @Operation(summary = "Bireysel müşteri listesi",
               description = "Sayfalanmış bireysel müşteri listesini döner.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Liste başarıyla döndü"),
            @ApiResponse(responseCode = "401", description = "Kimlik doğrulama gerekli",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(ref = "#/components/schemas/ErrorResponse")))
    })
    @GetMapping
    public ResponseEntity<Paginate<IndividualCustomerResponse>> getList(
            @Parameter(description = "Sayfa indeksi (0 tabanlı)", example = "0")
            @RequestParam(defaultValue = "0") int pageIndex,
            @Parameter(description = "Sayfa başına kayıt sayısı", example = "10")
            @RequestParam(defaultValue = "10") int pageSize) {
        return ResponseEntity.ok(mediator.query(new GetListIndividualCustomerQuery(pageIndex, pageSize)));
    }
}
