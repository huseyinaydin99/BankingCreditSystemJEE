package tr.com.huseyinaydin.web.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import tr.com.huseyinaydin.application.cqrs.Mediator;
import tr.com.huseyinaydin.application.customers.commands.CreateIndividualCustomerCommand;
import tr.com.huseyinaydin.application.customers.dtos.CreatedIndividualCustomerResponse;
import tr.com.huseyinaydin.application.customers.dtos.IndividualCustomerResponse;
import tr.com.huseyinaydin.application.customers.queries.GetByIdIndividualCustomerQuery;
import tr.com.huseyinaydin.infrastructure.config.SecurityConfig;
import tr.com.huseyinaydin.infrastructure.security.JjwtJwtService;
import tr.com.huseyinaydin.infrastructure.security.TokenOptions;
import tr.com.huseyinaydin.sharedkernel.exception.ConflictException;
import tr.com.huseyinaydin.sharedkernel.exception.NotFoundException;
import tr.com.huseyinaydin.web.exception.GlobalExceptionHandler;
import tr.com.huseyinaydin.web.utils.TestJwtTokenFactory;

import java.time.LocalDate;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(IndividualCustomersController.class)
@Import({SecurityConfig.class, GlobalExceptionHandler.class, IndividualCustomersControllerTest.TestConfig.class})
public class IndividualCustomersControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private Mediator mediator;

    @Configuration
    static class TestConfig {
        @Bean
        public TokenOptions tokenOptions() {
            TokenOptions options = new TokenOptions();
            org.springframework.test.util.ReflectionTestUtils.setField(options, "secretKey", TestJwtTokenFactory.SECRET);
            org.springframework.test.util.ReflectionTestUtils.setField(options, "issuer", TestJwtTokenFactory.ISSUER);
            org.springframework.test.util.ReflectionTestUtils.setField(options, "audience", TestJwtTokenFactory.AUDIENCE);
            org.springframework.test.util.ReflectionTestUtils.setField(options, "accessTokenExpiration", 60);
            return options;
        }

        @Bean
        public JjwtJwtService jwtService(TokenOptions tokenOptions) {
            return new JjwtJwtService(tokenOptions);
        }
    }

    @Test
    @DisplayName("POST /api/individual-customers başarılı - 201 Created")
    void testCreateSuccess() throws Exception {
        CreateIndividualCustomerCommand command = new CreateIndividualCustomerCommand(
                "Ali", "Veli", "12345678901", LocalDate.of(1990, 1, 1),
                "Ayse", "Mehmet", "+905551234567", "ali@veli.com", "Adres", "Pass123*"
        );

        CreatedIndividualCustomerResponse response = new CreatedIndividualCustomerResponse(
                UUID.randomUUID(), "Ali", "Veli", "12345678901", "ali@veli.com", "Success"
        );

        given(mediator.send(any(CreateIndividualCustomerCommand.class))).willReturn(response);

        String token = TestJwtTokenFactory.generateOfficerToken();

        mockMvc.perform(post("/api/individual-customers")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(command)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(response.id().toString()))
                .andExpect(jsonPath("$.firstName").value("Ali"))
                .andExpect(jsonPath("$.nationalId").value("12345678901"));
    }

    @Test
    @DisplayName("POST /api/individual-customers geçersiz body - 400 Bad Request")
    void testCreateInvalidBody() throws Exception {
        // Missing firstName, lastName, nationalId
        CreateIndividualCustomerCommand command = new CreateIndividualCustomerCommand(
                "", "", "", LocalDate.of(1990, 1, 1),
                "Ayse", "Mehmet", "123", "invalid-email", "Adres", "1"
        );

        String token = TestJwtTokenFactory.generateOfficerToken();

        mockMvc.perform(post("/api/individual-customers")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(command)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.title").value("Validation Error"))
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.errors").exists());
    }

    @Test
    @DisplayName("POST /api/individual-customers tc kimlik tekrarı - 409 Conflict")
    void testCreateDuplicateNationalId() throws Exception {
        CreateIndividualCustomerCommand command = new CreateIndividualCustomerCommand(
                "Ali", "Veli", "12345678901", LocalDate.of(1990, 1, 1),
                "Ayse", "Mehmet", "+905551234567", "ali@veli.com", "Adres", "Pass123*"
        );

        given(mediator.send(any(CreateIndividualCustomerCommand.class)))
                .willThrow(new ConflictException("nationalId", "12345678901", "Bu TC Kimlik No zaten kayıtlı"));

        String token = TestJwtTokenFactory.generateOfficerToken();

        mockMvc.perform(post("/api/individual-customers")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(command)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.title").value("Conflict"))
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.detail").value("Bu TC Kimlik No zaten kayıtlı"));
    }

    @Test
    @DisplayName("GET /api/individual-customers/{id} mevcut kayıt - 200 OK")
    void testGetByIdSuccess() throws Exception {
        UUID id = UUID.randomUUID();
        IndividualCustomerResponse response = new IndividualCustomerResponse(
                id, "Ali", "Veli", "12345678901", LocalDate.of(1990, 1, 1),
                "Ayse", "Mehmet", "+905551234567", "ali@veli.com", "Adres", true, null, null
        );

        given(mediator.query(any(GetByIdIndividualCustomerQuery.class))).willReturn(response);

        String token = TestJwtTokenFactory.generateOfficerToken();

        mockMvc.perform(get("/api/individual-customers/{id}", id)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(id.toString()))
                .andExpect(jsonPath("$.firstName").value("Ali"));
    }

    @Test
    @DisplayName("GET /api/individual-customers/{id} bulunamadı - 404 Not Found")
    void testGetByIdNotFound() throws Exception {
        UUID id = UUID.randomUUID();
        given(mediator.query(any(GetByIdIndividualCustomerQuery.class)))
                .willThrow(new NotFoundException("INDIVIDUAL_CUSTOMER", id.toString()));

        String token = TestJwtTokenFactory.generateOfficerToken();

        mockMvc.perform(get("/api/individual-customers/{id}", id)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.title").value("Not Found"))
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.detail").value("INDIVIDUAL_CUSTOMER entity with id " + id + " not found"));
    }

    @Test
    @DisplayName("Yetkilendirilmemiş (tokensiz) istek - 401 Unauthorized")
    void testUnauthorizedRequest() throws Exception {
        mockMvc.perform(get("/api/individual-customers/{id}", UUID.randomUUID()))
                .andExpect(status().isUnauthorized());
    }
}
