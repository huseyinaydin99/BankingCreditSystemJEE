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
import tr.com.huseyinaydin.application.credittype.commands.CreateCreditTypeCommand;
import tr.com.huseyinaydin.application.credittype.commands.DeleteCreditTypeCommand;
import tr.com.huseyinaydin.application.credittype.commands.UpdateCreditTypeCommand;
import tr.com.huseyinaydin.application.credittype.dtos.CreditTypeResponse;
import tr.com.huseyinaydin.application.credittype.queries.GetByIdCreditTypeQuery;
import tr.com.huseyinaydin.application.credittype.queries.GetListCreditTypeQuery;
import tr.com.huseyinaydin.domain.enums.CustomerType;
import tr.com.huseyinaydin.sharedkernel.pagination.Paginate;

import java.util.UUID;

@Tag(name = "Kredi Türleri", description = "Kredi ürün türü tanımlama, güncelleme, silme ve sorgulama işlemleri")
@SecurityRequirement(name = "bearerAuth")
@RestController
@RequestMapping("/api/credit-types")
public class CreditTypesController extends BaseController {

    public CreditTypesController(Mediator mediator) {
        super(mediator);
    }

    @Operation(summary = "Kredi türü oluştur",
               description = "Sistemde kullanılacak yeni bir kredi ürün türü tanımlar. Yalnızca ADMIN rolü erişebilir.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Kredi türü başarıyla oluşturuldu"),
            @ApiResponse(responseCode = "400", description = "Doğrulama hatası",
                    content = @Content(mediaType = MediaType.APPLICATION_PROBLEM_JSON_VALUE,
                            schema = @Schema(ref = "#/components/schemas/ValidationProblemDetail"))),
            @ApiResponse(responseCode = "401", description = "Kimlik doğrulama gerekli",
                    content = @Content(mediaType = MediaType.APPLICATION_PROBLEM_JSON_VALUE,
                            schema = @Schema(ref = "#/components/schemas/BusinessProblemDetail")))
    })
    @PostMapping
    public ResponseEntity<CreateCreditTypeCommand.Response> create(
            @RequestBody @Valid CreateCreditTypeCommand command) {
        return ResponseEntity.status(HttpStatus.CREATED).body(mediator.send(command));
    }

    @Operation(summary = "Kredi türlerini listele",
               description = "Kredi türlerini sayfalanmış olarak döner. parentCreditTypeId boş ise kök türleri, "
                       + "değer verilirse belirtilen ebeveynin alt türlerini döner. customerType ile ayrıca filtrelenebilir.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Liste başarıyla döndü"),
            @ApiResponse(responseCode = "401", description = "Kimlik doğrulama gerekli",
                    content = @Content(mediaType = MediaType.APPLICATION_PROBLEM_JSON_VALUE,
                            schema = @Schema(ref = "#/components/schemas/BusinessProblemDetail")))
    })
    @GetMapping
    public ResponseEntity<Paginate<CreditTypeResponse>> getList(
            @Parameter(description = "Müşteri türüne göre filtre: INDIVIDUAL veya CORPORATE (opsiyonel)")
            @RequestParam(required = false) CustomerType customerType,
            @Parameter(description = "Ebeveyn kredi türü UUID'si. Boş ise kök türler döner (opsiyonel)")
            @RequestParam(required = false) UUID parentCreditTypeId,
            @Parameter(description = "Sayfa indeksi (0 tabanlı)", example = "0")
            @RequestParam(defaultValue = "0") int pageIndex,
            @Parameter(description = "Sayfa başına kayıt sayısı", example = "10")
            @RequestParam(defaultValue = "10") int pageSize) {
        return ResponseEntity.ok(mediator.query(
                new GetListCreditTypeQuery(customerType, parentCreditTypeId, pageIndex, pageSize)));
    }

    @Operation(summary = "Kredi türünü id ile getir",
               description = "Belirtilen kredi türünü ve alt türlerini döner.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Kredi türü döndü"),
            @ApiResponse(responseCode = "401", description = "Kimlik doğrulama gerekli",
                    content = @Content(mediaType = MediaType.APPLICATION_PROBLEM_JSON_VALUE,
                            schema = @Schema(ref = "#/components/schemas/BusinessProblemDetail"))),
            @ApiResponse(responseCode = "404", description = "Kredi türü bulunamadı",
                    content = @Content(mediaType = MediaType.APPLICATION_PROBLEM_JSON_VALUE,
                            schema = @Schema(ref = "#/components/schemas/BusinessProblemDetail")))
    })
    @GetMapping("/{id}")
    public ResponseEntity<CreditTypeResponse> getById(
            @Parameter(description = "Kredi türünün UUID'si", required = true)
            @PathVariable UUID id) {
        return ResponseEntity.ok(mediator.query(new GetByIdCreditTypeQuery(id)));
    }

    @Operation(summary = "Kredi türünü güncelle",
               description = "Var olan bir kredi türünün tutar/vade/faiz ve ebeveyn bilgilerini günceller.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Kredi türü güncellendi"),
            @ApiResponse(responseCode = "400", description = "Doğrulama hatası",
                    content = @Content(mediaType = MediaType.APPLICATION_PROBLEM_JSON_VALUE,
                            schema = @Schema(ref = "#/components/schemas/ValidationProblemDetail"))),
            @ApiResponse(responseCode = "401", description = "Kimlik doğrulama gerekli",
                    content = @Content(mediaType = MediaType.APPLICATION_PROBLEM_JSON_VALUE,
                            schema = @Schema(ref = "#/components/schemas/BusinessProblemDetail"))),
            @ApiResponse(responseCode = "404", description = "Kredi türü bulunamadı",
                    content = @Content(mediaType = MediaType.APPLICATION_PROBLEM_JSON_VALUE,
                            schema = @Schema(ref = "#/components/schemas/BusinessProblemDetail")))
    })
    @PutMapping("/{id}")
    public ResponseEntity<UpdateCreditTypeCommand.Response> update(
            @Parameter(description = "Kredi türünün UUID'si", required = true)
            @PathVariable UUID id,
            @RequestBody @Valid UpdateCreditTypeCommand.Request request) {
        return ResponseEntity.ok(mediator.send(new UpdateCreditTypeCommand(
                id, request.name(), request.description(),
                request.minimumAmount(), request.maximumAmount(),
                request.minimumTermMonths(), request.maximumTermMonths(),
                request.annualInterestRate(), request.parentCreditTypeId())));
    }

    @Operation(summary = "Kredi türünü sil",
               description = "Kredi türünü soft delete ile siler. Alt kredi türü bulunan tür silinemez.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Kredi türü silindi"),
            @ApiResponse(responseCode = "400", description = "Alt kredi türü olan tür silinemez",
                    content = @Content(mediaType = MediaType.APPLICATION_PROBLEM_JSON_VALUE,
                            schema = @Schema(ref = "#/components/schemas/BusinessProblemDetail"))),
            @ApiResponse(responseCode = "401", description = "Kimlik doğrulama gerekli",
                    content = @Content(mediaType = MediaType.APPLICATION_PROBLEM_JSON_VALUE,
                            schema = @Schema(ref = "#/components/schemas/BusinessProblemDetail"))),
            @ApiResponse(responseCode = "404", description = "Kredi türü bulunamadı",
                    content = @Content(mediaType = MediaType.APPLICATION_PROBLEM_JSON_VALUE,
                            schema = @Schema(ref = "#/components/schemas/BusinessProblemDetail")))
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<DeleteCreditTypeCommand.Response> delete(
            @Parameter(description = "Kredi türünün UUID'si", required = true)
            @PathVariable UUID id) {
        return ResponseEntity.ok(mediator.send(new DeleteCreditTypeCommand(id)));
    }
}
