package tr.com.huseyinaydin.persistence.read;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import tr.com.huseyinaydin.application.customers.dtos.IndividualCustomerResponse;
import tr.com.huseyinaydin.application.ports.read.IIndividualCustomerReadService;

import java.sql.Timestamp;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class IndividualCustomerReadServiceImpl implements IIndividualCustomerReadService {

    private final JdbcTemplate jdbcTemplate;

    public IndividualCustomerReadServiceImpl(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public Optional<IndividualCustomerResponse> getById(UUID id) {
        String sql = "SELECT c.ID, ic.FIRST_NAME, ic.LAST_NAME, ic.NATIONAL_ID, ic.DATE_OF_BIRTH, " +
                     "ic.MOTHER_NAME, ic.FATHER_NAME, c.PHONE_NUMBER, c.EMAIL, c.ADDRESS, " +
                     "c.IS_ACTIVE, c.CREATED_DATE, c.UPDATED_DATE " +
                     "FROM INDIVIDUAL_CUSTOMERS ic " +
                     "JOIN CUSTOMERS c ON ic.ID = c.ID " +
                     "WHERE c.ID = ? AND c.DELETED_DATE IS NULL";

        List<IndividualCustomerResponse> results = jdbcTemplate.query(sql, (rs, rowNum) -> {
            Timestamp dobTs = rs.getTimestamp("DATE_OF_BIRTH");
            Timestamp createdTs = rs.getTimestamp("CREATED_DATE");
            Timestamp updatedTs = rs.getTimestamp("UPDATED_DATE");

            return new IndividualCustomerResponse(
                    UUID.fromString(rs.getString("ID")),
                    rs.getString("FIRST_NAME"),
                    rs.getString("LAST_NAME"),
                    rs.getString("NATIONAL_ID"),
                    dobTs != null ? dobTs.toLocalDateTime().toLocalDate() : null,
                    rs.getString("MOTHER_NAME"),
                    rs.getString("FATHER_NAME"),
                    rs.getString("PHONE_NUMBER"),
                    rs.getString("EMAIL"),
                    rs.getString("ADDRESS"),
                    rs.getBoolean("IS_ACTIVE"),
                    createdTs != null ? createdTs.toLocalDateTime() : null,
                    updatedTs != null ? updatedTs.toLocalDateTime() : null
            );
        }, id.toString());

        return results.isEmpty() ? Optional.empty() : Optional.of(results.get(0));
    }
}
