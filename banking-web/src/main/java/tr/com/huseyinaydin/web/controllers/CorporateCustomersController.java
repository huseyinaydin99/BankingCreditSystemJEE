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

@Tag(name = "Kurumsal Müşteriler", description = "Kurumsal müşteri oluşturma, güncelleme, silme ve listeleme işlemleri")
@SecurityRequirement(name = "bearerAuth")
@RestController
@RequestMapping("/api/corporate-customers")
public class CorporateCustomersController extends BaseController {

    public CorporateCustomersController(Mediator mediator) {
        super(mediator);
    }

    @Operation(summary = "Kurumsal müşteri oluştur",
               description = "Yeni bir kurumsal müşteri kaydı oluşturur. Vergi numarası benzersiz olmalıdır.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Müşteri başarıyla oluşturuldu"),
            @ApiResponse(responseCode = "400", description = "Doğrulama hatası",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(ref = "#/components/schemas/ValidationErrorResponse"))),
            @ApiResponse(responseCode = "401", description = "Kimlik doğrulama gerekli",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(ref = "#/components/schemas/ErrorResponse"))),
            @ApiResponse(responseCode = "409", description = "Vergi numarası zaten kayıtlı",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(ref = "#/components/schemas/ErrorResponse")))
    })
    @PostMapping
    public ResponseEntity<CreatedCorporateCustomerResponse> create(
            @RequestBody @Valid CreateCorporateCustomerCommand command) {
        return ResponseEntity.status(HttpStatus.CREATED).body(mediator.send(command));
    }

    @Operation(summary = "Kurumsal müşteri güncelle",
               description = "Mevcut kurumsal müşterinin şirket ve iletişim bilgilerini günceller.")
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
    public ResponseEntity<UpdatedCorporateCustomerResponse> update(
            @Parameter(description = "Güncellenecek müşterinin UUID'si", required = true)
            @PathVariable UUID id,
            @RequestBody @Valid UpdateCorporateCustomerCommand body) {
        UpdateCorporateCustomerCommand cmd = new UpdateCorporateCustomerCommand(
                id, body.companyName(), body.taxOffice(),
                body.companyRegistrationNumber(), body.authorizedPersonName(),
                body.companyFoundationDate(), body.phoneNumber(), body.email(), body.address()
        );
        return ResponseEntity.ok(mediator.send(cmd));
    }

    @Operation(summary = "Kurumsal müşteri sil",
               description = "Belirtilen kurumsal müşteriyi soft-delete ile pasifleştirir.")
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
    public ResponseEntity<DeletedCorporateCustomerResponse> delete(
            @Parameter(description = "Silinecek müşterinin UUID'si", required = true)
            @PathVariable UUID id) {
        return ResponseEntity.status(HttpStatus.NO_CONTENT)
                .body(mediator.send(new DeleteCorporateCustomerCommand(id)));
    }

    @Operation(summary = "ID ile kurumsal müşteri getir",
               description = "UUID ile tekil kurumsal müşteri kaydını döner.")
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
    public ResponseEntity<CorporateCustomerResponse> getById(
            @Parameter(description = "Getirilecek müşterinin UUID'si", required = true)
            @PathVariable UUID id) {
        return ResponseEntity.ok(mediator.query(new GetByIdCorporateCustomerQuery(id)));
    }

    @Operation(summary = "Kurumsal müşteri listesi",
               description = "Sayfalanmış kurumsal müşteri listesini döner.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Liste başarıyla döndü"),
            @ApiResponse(responseCode = "401", description = "Kimlik doğrulama gerekli",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(ref = "#/components/schemas/ErrorResponse")))
    })
    @GetMapping
    public ResponseEntity<Paginate<CorporateCustomerResponse>> getList(
            @Parameter(description = "Sayfa indeksi (0 tabanlı)", example = "0")
            @RequestParam(defaultValue = "0") int pageIndex,
            @Parameter(description = "Sayfa başına kayıt sayısı", example = "10")
            @RequestParam(defaultValue = "10") int pageSize) {
        return ResponseEntity.ok(mediator.query(new GetListCorporateCustomerQuery(pageIndex, pageSize)));
    }
}
