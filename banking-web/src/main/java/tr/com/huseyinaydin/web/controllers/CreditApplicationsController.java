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
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import tr.com.huseyinaydin.application.cqrs.Mediator;
import tr.com.huseyinaydin.application.creditapplication.commands.CreateCreditApplicationCommand;
import tr.com.huseyinaydin.application.creditapplication.dtos.CreditApplicationResponse;
import tr.com.huseyinaydin.application.creditapplication.queries.GetListByCustomerCreditApplicationQuery;
import tr.com.huseyinaydin.sharedkernel.pagination.Paginate;

import java.util.UUID;

@Tag(name = "Kredi Başvuruları", description = "Kredi başvurusu oluşturma ve müşteri bazlı sorgulama işlemleri")
@SecurityRequirement(name = "bearerAuth")
@RestController
@RequestMapping("/api/credit-applications")
public class CreditApplicationsController extends BaseController {

    public CreditApplicationsController(Mediator mediator) {
        super(mediator);
    }

    @Operation(summary = "Kredi başvurusu oluştur",
               description = "Belirtilen müşteri için yeni bir kredi başvurusu oluşturur. Başvuru PENDING durumunda oluşturulur.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Başvuru başarıyla oluşturuldu"),
            @ApiResponse(responseCode = "400", description = "Doğrulama hatası",
                    content = @Content(mediaType = MediaType.APPLICATION_PROBLEM_JSON_VALUE,
                            schema = @Schema(ref = "#/components/schemas/ValidationProblemDetail"))),
            @ApiResponse(responseCode = "401", description = "Kimlik doğrulama gerekli",
                    content = @Content(mediaType = MediaType.APPLICATION_PROBLEM_JSON_VALUE,
                            schema = @Schema(ref = "#/components/schemas/BusinessProblemDetail"))),
            @ApiResponse(responseCode = "404", description = "Müşteri veya kredi türü bulunamadı",
                    content = @Content(mediaType = MediaType.APPLICATION_PROBLEM_JSON_VALUE,
                            schema = @Schema(ref = "#/components/schemas/BusinessProblemDetail")))
    })
    @PostMapping
    public ResponseEntity<CreateCreditApplicationCommand.Response> create(
            @RequestBody @Valid CreateCreditApplicationCommand command) {
        return ResponseEntity.status(HttpStatus.CREATED).body(mediator.send(command));
    }

    @Operation(summary = "Müşteriye ait kredi başvurularını listele",
               description = "Belirtilen müşteriye ait tüm kredi başvurularını sayfalanmış olarak döner.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Liste başarıyla döndü"),
            @ApiResponse(responseCode = "401", description = "Kimlik doğrulama gerekli",
                    content = @Content(mediaType = MediaType.APPLICATION_PROBLEM_JSON_VALUE,
                            schema = @Schema(ref = "#/components/schemas/BusinessProblemDetail"))),
            @ApiResponse(responseCode = "404", description = "Müşteri bulunamadı",
                    content = @Content(mediaType = MediaType.APPLICATION_PROBLEM_JSON_VALUE,
                            schema = @Schema(ref = "#/components/schemas/BusinessProblemDetail")))
    })
    @GetMapping("/customer/{customerId}")
    public ResponseEntity<Paginate<CreditApplicationResponse>> getByCustomer(
            @Parameter(description = "Başvuruları listelenecek müşterinin UUID'si", required = true)
            @PathVariable UUID customerId,
            @Parameter(description = "Sayfa indeksi (0 tabanlı)", example = "0")
            @RequestParam(defaultValue = "0") int pageIndex,
            @Parameter(description = "Sayfa başına kayıt sayısı", example = "10")
            @RequestParam(defaultValue = "10") int pageSize) {
        return ResponseEntity.ok(mediator.query(
                new GetListByCustomerCreditApplicationQuery(customerId, pageIndex, pageSize)));
    }
}
