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
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import tr.com.huseyinaydin.application.cqrs.Mediator;
import tr.com.huseyinaydin.application.creditapplication.commands.ApproveCreditApplicationCommand;
import tr.com.huseyinaydin.application.creditapplication.commands.MoveCreditApplicationToReviewCommand;
import tr.com.huseyinaydin.application.creditapplication.commands.RejectCreditApplicationCommand;

import java.util.UUID;

/**
 * Kredi başvurusu onay iş akışı endpoint'leri: incele → onayla / reddet.
 * Durum makinesi PENDING → UNDER_REVIEW → APPROVED/REJECTED çizgisinde ilerler;
 * geçersiz geçişler RFC 7807 "business-rule-violation" olarak döner.
 */
@Tag(name = "Kredi Başvuru Onayı", description = "Kredi başvurusu inceleme, onay ve ret iş akışı")
@SecurityRequirement(name = "bearerAuth")
@RestController
@RequestMapping("/api/credit-applications")
public class ApproveCreditApplicationsController extends BaseController {

    public ApproveCreditApplicationsController(Mediator mediator) {
        super(mediator);
    }

    @Operation(summary = "Başvuruyu incelemeye al",
               description = "Başvuruyu PENDING durumundan UNDER_REVIEW durumuna taşır. "
                       + "Onay/ret adımlarının ön koşuludur.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Başvuru incelemeye alındı"),
            @ApiResponse(responseCode = "400", description = "Geçersiz durum geçişi",
                    content = @Content(mediaType = MediaType.APPLICATION_PROBLEM_JSON_VALUE,
                            schema = @Schema(ref = "#/components/schemas/BusinessProblemDetail"))),
            @ApiResponse(responseCode = "401", description = "Kimlik doğrulama gerekli",
                    content = @Content(mediaType = MediaType.APPLICATION_PROBLEM_JSON_VALUE,
                            schema = @Schema(ref = "#/components/schemas/BusinessProblemDetail"))),
            @ApiResponse(responseCode = "404", description = "Başvuru bulunamadı",
                    content = @Content(mediaType = MediaType.APPLICATION_PROBLEM_JSON_VALUE,
                            schema = @Schema(ref = "#/components/schemas/BusinessProblemDetail")))
    })
    @PutMapping("/{id}/review")
    public ResponseEntity<MoveCreditApplicationToReviewCommand.Response> review(
            @Parameter(description = "Başvurunun UUID'si", required = true)
            @PathVariable UUID id) {
        return ResponseEntity.ok(mediator.send(new MoveCreditApplicationToReviewCommand(id)));
    }

    @Operation(summary = "Başvuruyu onayla",
               description = "UNDER_REVIEW durumundaki başvuruyu onaylar; onaylanan tutar/vade/faiz "
                       + "ile anüite ödeme planı (aylık taksit, toplam ödeme) hesaplanır.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Başvuru onaylandı"),
            @ApiResponse(responseCode = "400", description = "Doğrulama hatası veya geçersiz durum geçişi",
                    content = @Content(mediaType = MediaType.APPLICATION_PROBLEM_JSON_VALUE,
                            schema = @Schema(ref = "#/components/schemas/BusinessProblemDetail"))),
            @ApiResponse(responseCode = "401", description = "Kimlik doğrulama gerekli",
                    content = @Content(mediaType = MediaType.APPLICATION_PROBLEM_JSON_VALUE,
                            schema = @Schema(ref = "#/components/schemas/BusinessProblemDetail"))),
            @ApiResponse(responseCode = "404", description = "Başvuru bulunamadı",
                    content = @Content(mediaType = MediaType.APPLICATION_PROBLEM_JSON_VALUE,
                            schema = @Schema(ref = "#/components/schemas/BusinessProblemDetail")))
    })
    @PutMapping("/{id}/approve")
    public ResponseEntity<ApproveCreditApplicationCommand.Response> approve(
            @Parameter(description = "Başvurunun UUID'si", required = true)
            @PathVariable UUID id,
            @RequestBody @Valid ApproveCreditApplicationCommand.Request request) {
        return ResponseEntity.ok(mediator.send(new ApproveCreditApplicationCommand(
                id, request.approvedAmount(), request.approvedTerm(), request.interestRate())));
    }

    @Operation(summary = "Başvuruyu reddet",
               description = "UNDER_REVIEW durumundaki başvuruyu, ret gerekçesiyle reddeder.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Başvuru reddedildi"),
            @ApiResponse(responseCode = "400", description = "Doğrulama hatası veya geçersiz durum geçişi",
                    content = @Content(mediaType = MediaType.APPLICATION_PROBLEM_JSON_VALUE,
                            schema = @Schema(ref = "#/components/schemas/BusinessProblemDetail"))),
            @ApiResponse(responseCode = "401", description = "Kimlik doğrulama gerekli",
                    content = @Content(mediaType = MediaType.APPLICATION_PROBLEM_JSON_VALUE,
                            schema = @Schema(ref = "#/components/schemas/BusinessProblemDetail"))),
            @ApiResponse(responseCode = "404", description = "Başvuru bulunamadı",
                    content = @Content(mediaType = MediaType.APPLICATION_PROBLEM_JSON_VALUE,
                            schema = @Schema(ref = "#/components/schemas/BusinessProblemDetail")))
    })
    @PutMapping("/{id}/reject")
    public ResponseEntity<RejectCreditApplicationCommand.Response> reject(
            @Parameter(description = "Başvurunun UUID'si", required = true)
            @PathVariable UUID id,
            @RequestBody @Valid RejectCreditApplicationCommand.Request request) {
        return ResponseEntity.ok(mediator.send(new RejectCreditApplicationCommand(
                id, request.rejectionReason())));
    }
}
