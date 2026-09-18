package tr.com.huseyinaydin.persistence.read;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import tr.com.huseyinaydin.application.creditapplication.dtos.CreditApplicationResponse;
import tr.com.huseyinaydin.application.ports.read.ICreditApplicationReadService;
import tr.com.huseyinaydin.domain.enums.CreditApplicationStatus;
import tr.com.huseyinaydin.sharedkernel.pagination.Paginate;

import java.sql.Timestamp;
import java.util.List;
import java.util.UUID;

@Service
public class CreditApplicationReadServiceImpl implements ICreditApplicationReadService {

    private final JdbcTemplate jdbcTemplate;

    public CreditApplicationReadServiceImpl(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public Paginate<CreditApplicationResponse> getListByCustomerId(UUID customerId, int pageIndex, int pageSize) {
        String countSql = "SELECT COUNT(*) FROM CREDIT_APPLICATIONS WHERE CUSTOMER_ID = ? AND DELETED_DATE IS NULL";
        Long totalCount = jdbcTemplate.queryForObject(countSql, Long.class, customerId.toString());
        long total = totalCount != null ? totalCount : 0L;

        String customerSql = "SELECT c.CUSTOMER_TYPE_CODE, i.FIRST_NAME, i.LAST_NAME, co.COMPANY_NAME " +
                             "FROM CUSTOMERS c " +
                             "LEFT JOIN INDIVIDUAL_CUSTOMERS i ON i.ID = c.ID " +
                             "LEFT JOIN CORPORATE_CUSTOMERS co ON co.ID = c.ID " +
                             "WHERE c.ID = ?";
        
        List<String> customerNames = jdbcTemplate.query(customerSql, (rs, rowNum) -> {
            String type = rs.getString("CUSTOMER_TYPE_CODE");
            if ("INDIVIDUAL".equals(type)) {
                return rs.getString("FIRST_NAME") + " " + rs.getString("LAST_NAME");
            } else {
                return rs.getString("COMPANY_NAME");
            }
        }, customerId.toString());
        
        String customerName = customerNames.isEmpty() ? "" : customerNames.get(0);

        String sql = "SELECT ca.ID as ca_id, ca.CUSTOMER_ID as ca_customer_id, ca.CREDIT_TYPE_ID as ca_credit_type_id, " +
                     "ca.REQUESTED_AMOUNT as ca_req_amount, ca.REQUESTED_TERM_MONTHS as ca_req_term, " +
                     "ca.APPROVED_AMOUNT as ca_app_amount, ca.INTEREST_RATE as ca_interest_rate, " +
                     "ca.MONTHLY_PAYMENT as ca_monthly, ca.TOTAL_PAYMENT as ca_total, " +
                     "ca.STATUS as ca_status, ca.REJECTION_REASON as ca_reason, ca.CREATED_DATE as ca_created, " +
                     "ct.NAME as ct_name " +
                     "FROM CREDIT_APPLICATIONS ca " +
                     "JOIN CREDIT_TYPES ct ON ca.CREDIT_TYPE_ID = ct.ID " +
                     "WHERE ca.CUSTOMER_ID = ? AND ca.DELETED_DATE IS NULL " +
                     "ORDER BY ca.CREATED_DATE DESC " +
                     "OFFSET ? ROWS FETCH NEXT ? ROWS ONLY";

        int offset = pageIndex * pageSize;
        
        List<CreditApplicationResponse> items = jdbcTemplate.query(sql, (rs, rowNum) -> {
            Timestamp createdTs = rs.getTimestamp("ca_created");
            String statusStr = rs.getString("ca_status");

            return new CreditApplicationResponse(
                    UUID.fromString(rs.getString("ca_id")),
                    UUID.fromString(rs.getString("ca_customer_id")),
                    customerName,
                    UUID.fromString(rs.getString("ca_credit_type_id")),
                    rs.getString("ct_name"),
                    rs.getBigDecimal("ca_req_amount"),
                    rs.getInt("ca_req_term"),
                    rs.getBigDecimal("ca_app_amount"),
                    rs.getBigDecimal("ca_interest_rate"),
                    rs.getBigDecimal("ca_monthly"),
                    rs.getBigDecimal("ca_total"),
                    statusStr != null ? CreditApplicationStatus.valueOf(statusStr) : null,
                    rs.getString("ca_reason"),
                    createdTs != null ? createdTs.toLocalDateTime() : null
            );
        }, customerId.toString(), offset, pageSize);

        return new Paginate<>(items, pageIndex, pageSize, total);
    }
}
