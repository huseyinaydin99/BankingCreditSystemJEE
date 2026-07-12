package tr.com.huseyinaydin.web.config;

import io.swagger.v3.core.converter.ModelConverters;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.media.IntegerSchema;
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
 * RFC 7807 Problem Details şemaları (ProblemDetail / BusinessProblemDetail /
 * ValidationProblemDetail) programatik olarak tanımlanır;
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
     * RFC 7807 "Problem Details" şemaları burada programatik olarak tanımlanır.
     * Bu sayede shared-kernel modülüne swagger-annotations bağımlılığı girmez;
     * @Schema yerine OpenApiCustomizer kullanımı katman sınırını korur.
     *
     * Yayınlanan tipler:
     *   - ProblemDetail            : RFC 7807 temel modeli (type/title/status/detail/instance)
     *   - BusinessProblemDetail    : ProblemDetail + errorCode (400/401/404/409/500)
     *   - ValidationProblemDetail  : ProblemDetail + errors (alan → mesaj listesi)
     */
    @Bean
    public OpenApiCustomizer errorSchemaCustomizer() {
        return openApi -> {
            if (openApi.getComponents() == null) {
                openApi.setComponents(new Components());
            }

            Schema<Object> problemDetailSchema = new ObjectSchema()
                    .description("RFC 7807 Problem Details temel modeli (application/problem+json)")
                    .addProperty("type", new StringSchema()
                            .format("uri")
                            .description("Problemi tanımlayan URI referansı")
                            .example("/problems/business-rule-violation"))
                    .addProperty("title", new StringSchema()
                            .description("Problem tipinin kısa, insan-okur özeti")
                            .example("İş Kuralı İhlali"))
                    .addProperty("status", new IntegerSchema()
                            .description("HTTP durum kodu")
                            .example(400))
                    .addProperty("detail", new StringSchema()
                            .description("Bu spesifik olaya özgü açıklama")
                            .example("Bu TC Kimlik Numarası zaten kayıtlı"))
                    .addProperty("instance", new StringSchema()
                            .format("uri")
                            .description("Problemin oluştuğu istek yolu")
                            .example("/api/individual-customers"));
            problemDetailSchema.setRequired(List.of("type", "title", "status"));

            Schema<Object> businessProblemDetailSchema = new ObjectSchema()
                    .description("Uygulama hata kodu taşıyan Problem Details modeli "
                            + "(400 / 401 / 404 / 409 / 500)");
            businessProblemDetailSchema.addAllOfItem(
                    new Schema<>().$ref("#/components/schemas/ProblemDetail"));
            businessProblemDetailSchema.addAllOfItem(new ObjectSchema()
                    .addProperty("errorCode", new StringSchema()
                            .description("Programatik olarak ayırt edilebilen, dile bağlı olmayan hata kodu")
                            .example("BUSINESS_RULE_VIOLATION")));

            Schema<Object> validationProblemDetailSchema = new ObjectSchema()
                    .description("Çoklu alan doğrulama Problem Details modeli (400)");
            validationProblemDetailSchema.addAllOfItem(
                    new Schema<>().$ref("#/components/schemas/ProblemDetail"));
            validationProblemDetailSchema.addAllOfItem(new ObjectSchema()
                    .addProperty("errors", new ObjectSchema()
                            .additionalProperties(new io.swagger.v3.oas.models.media.ArraySchema()
                                    .items(new StringSchema()))
                            .description("Alan adı → o alana ait hata mesajları listesi")));

            openApi.getComponents()
                    .addSchemas("ProblemDetail", problemDetailSchema)
                    .addSchemas("BusinessProblemDetail", businessProblemDetailSchema)
                    .addSchemas("ValidationProblemDetail", validationProblemDetailSchema);
        };
    }
}
