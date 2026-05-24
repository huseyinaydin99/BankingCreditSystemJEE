package tr.com.huseyinaydin.web.config;

import io.swagger.v3.core.converter.ModelConverters;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.media.DateTimeSchema;
import io.swagger.v3.oas.models.media.ObjectSchema;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.media.StringSchema;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springdoc.core.configuration.SpringDocConfiguration;
import org.springdoc.core.customizers.OpenApiCustomizer;
import org.springdoc.webmvc.core.configuration.SpringDocWebMvcConfiguration;
import org.springdoc.webmvc.ui.SwaggerConfig;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

import java.util.List;

/*
 * Spring Boot olmayan (pure Spring MVC 6) bir uygulamada springdoc entegrasyonu.
 * @Import ile springdoc'un @Configuration sınıfları manuel olarak yüklenir;
 * springdoc-openapi-starter-webmvc-ui bağımlılığı spring-boot-autoconfigure'ı
 * transitif olarak getirir — @EnableConfigurationProperties dolayısıyla çalışır.
 *
 * ErrorResponse ve ValidationErrorResponse şemaları programatik olarak tanımlanır;
 * bu sayede shared-kernel modülü dokümantasyon bağımlılıklarından temiz kalır.
 */
@Configuration
@Import({
        SpringDocConfiguration.class,
        SpringDocWebMvcConfiguration.class,
        SwaggerConfig.class
})
public class OpenApiConfig {

    @Bean
    public OpenAPI bankingOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Banking Credit System API")
                        .description("""
                                Bankacılık ve kredi sistemi REST API dokümantasyonu.
                                Clean Architecture (Shared Kernel → Domain → Application → Infrastructure → Web).
                                Tüm istekler JWT Bearer token gerektirir.
                                """)
                        .version("v1")
                        .contact(new Contact()
                                .name("Hüseyin Aydın")
                                .email("huseyinaydin@example.com")
                                .url("https://github.com/huseyinaydin99"))
                        .license(new License()
                                .name("MIT License")
                                .url("https://opensource.org/licenses/MIT")))
                .components(new Components()
                        .addSecuritySchemes("bearerAuth", new SecurityScheme()
                                .type(SecurityScheme.Type.HTTP)
                                .scheme("bearer")
                                .bearerFormat("JWT")
                                .description("JWT erişim tokenı. Örnek: Bearer eyJhbGciOiJIUzUxMiJ9...")));
    }

    /*
     * ErrorResponse ve ValidationErrorResponse şemaları burada programatik olarak tanımlanır.
     * Bu sayede shared-kernel modülüne swagger-annotations bağımlılığı girmez.
     * @Schema yerine OpenApiCustomizer kullanımı katman sınırını korur.
     */
    @Bean
    public OpenApiCustomizer errorSchemaCustomizer() {
        return openApi -> {
            if (openApi.getComponents() == null) {
                openApi.setComponents(new Components());
            }

            Schema<Object> fieldErrorSchema = new ObjectSchema()
                    .description("Alan bazlı doğrulama hatası")
                    .addProperty("field", new StringSchema()
                            .description("Hatalı alan adı")
                            .example("nationalId"))
                    .addProperty("message", new StringSchema()
                            .description("Hata mesajı")
                            .example("Geçerli bir TC Kimlik Numarası giriniz"))
                    .addProperty("rejectedValue", new Schema<>()
                            .description("Reddedilen değer")
                            .example("123abc"));
            fieldErrorSchema.setRequired(List.of("field", "message"));

            Schema<Object> errorResponseSchema = new ObjectSchema()
                    .description("Genel hata yanıt modeli (400 / 401 / 404 / 409 / 500)")
                    .addProperty("errorCode", new StringSchema()
                            .description("Uygulama hata kodu")
                            .example("BUSINESS_RULE_VIOLATION"))
                    .addProperty("message", new StringSchema()
                            .description("Kullanıcıya okunabilir hata mesajı")
                            .example("Bu TC Kimlik Numarası zaten kayıtlı"))
                    .addProperty("timestamp", new DateTimeSchema()
                            .description("Hatanın oluştuğu zaman (ISO-8601)"))
                    .addProperty("path", new StringSchema()
                            .description("Hatanın oluştuğu istek yolu")
                            .example("/api/individual-customers"));
            errorResponseSchema.setRequired(List.of("errorCode", "message", "timestamp", "path"));

            Schema<Object> validationErrorResponseSchema = new ObjectSchema()
                    .description("Çoklu alan doğrulama hata yanıt modeli (400)")
                    .addProperty("errors", new io.swagger.v3.oas.models.media.ArraySchema()
                            .items(new Schema<>().$ref("#/components/schemas/FieldError"))
                            .description("Alan bazlı hata listesi"))
                    .addProperty("timestamp", new DateTimeSchema()
                            .description("Hatanın oluştuğu zaman (ISO-8601)"));
            validationErrorResponseSchema.setRequired(List.of("errors", "timestamp"));

            openApi.getComponents()
                    .addSchemas("FieldError", fieldErrorSchema)
                    .addSchemas("ErrorResponse", errorResponseSchema)
                    .addSchemas("ValidationErrorResponse", validationErrorResponseSchema);
        };
    }
}
