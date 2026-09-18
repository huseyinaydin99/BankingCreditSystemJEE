package tr.com.huseyinaydin.persistence.read;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import tr.com.huseyinaydin.application.credittype.dtos.CreditTypeResponse;
import tr.com.huseyinaydin.application.ports.read.ICreditTypeReadService;
import tr.com.huseyinaydin.domain.enums.CustomerType;
import tr.com.huseyinaydin.sharedkernel.pagination.Paginate;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
public class CreditTypeReadServiceImpl implements ICreditTypeReadService {

    private final JdbcTemplate jdbcTemplate;

    public CreditTypeReadServiceImpl(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public Paginate<CreditTypeResponse> getList(CustomerType customerType, UUID parentCreditTypeId, int pageIndex, int pageSize) {
        StringBuilder sql = new StringBuilder(
                "SELECT ID, NAME, DESCRIPTION, CUSTOMER_TYPE, MINIMUM_AMOUNT, MAXIMUM_AMOUNT, " +
                "MINIMUM_TERM_MONTHS, MAXIMUM_TERM_MONTHS, ANNUAL_INTEREST_RATE, " +
                "PARENT_CREDIT_TYPE_ID, CREATED_DATE " +
                "FROM CREDIT_TYPES WHERE DELETED_DATE IS NULL");
                
        StringBuilder countSql = new StringBuilder("SELECT COUNT(*) FROM CREDIT_TYPES WHERE DELETED_DATE IS NULL");

        List<Object> params = new ArrayList<>();

        if (parentCreditTypeId == null) {
            sql.append(" AND PARENT_CREDIT_TYPE_ID IS NULL");
            countSql.append(" AND PARENT_CREDIT_TYPE_ID IS NULL");
        } else {
            sql.append(" AND PARENT_CREDIT_TYPE_ID = ?");
            countSql.append(" AND PARENT_CREDIT_TYPE_ID = ?");
            params.add(parentCreditTypeId.toString());
        }

        if (customerType != null) {
            sql.append(" AND CUSTOMER_TYPE = ?");
            countSql.append(" AND CUSTOMER_TYPE = ?");
            params.add(customerType.name());
        }

        Long totalCount = jdbcTemplate.queryForObject(countSql.toString(), Long.class, params.toArray());
        long total = totalCount != null ? totalCount : 0L;

        int offset = pageIndex * pageSize;
        sql.append(" ORDER BY CREATED_DATE DESC OFFSET ? ROWS FETCH NEXT ? ROWS ONLY");
        params.add(offset);
        params.add(pageSize);

        List<CreditTypeResponse> items = jdbcTemplate.query(sql.toString(), (rs, rowNum) -> {
            Timestamp createdTs = rs.getTimestamp("CREATED_DATE");
            String parentIdStr = rs.getString("PARENT_CREDIT_TYPE_ID");
            String custTypeStr = rs.getString("CUSTOMER_TYPE");

            return new CreditTypeResponse(
                    UUID.fromString(rs.getString("ID")),
                    rs.getString("NAME"),
                    rs.getString("DESCRIPTION"),
                    custTypeStr != null ? CustomerType.valueOf(custTypeStr) : null,
                    rs.getBigDecimal("MINIMUM_AMOUNT"),
                    rs.getBigDecimal("MAXIMUM_AMOUNT"),
                    rs.getInt("MINIMUM_TERM_MONTHS"),
                    rs.getInt("MAXIMUM_TERM_MONTHS"),
                    rs.getBigDecimal("ANNUAL_INTEREST_RATE"),
                    parentIdStr != null ? UUID.fromString(parentIdStr) : null,
                    new ArrayList<>(),
                    createdTs != null ? createdTs.toLocalDateTime() : null
            );
        }, params.toArray());

        return new Paginate<>(
                items,
                pageIndex,
                pageSize,
                total
        );
    }
}
