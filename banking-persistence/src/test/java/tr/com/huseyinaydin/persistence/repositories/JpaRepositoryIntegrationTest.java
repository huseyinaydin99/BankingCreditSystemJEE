package tr.com.huseyinaydin.persistence.repositories;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.Predicate;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.transaction.annotation.Transactional;
import tr.com.huseyinaydin.domain.customer.IndividualCustomer;
import tr.com.huseyinaydin.domain.repositories.Specification;
import tr.com.huseyinaydin.infrastructure.repositories.IndividualCustomerJpaRepository;
import tr.com.huseyinaydin.persistence.config.TestPersistenceConfig;
import tr.com.huseyinaydin.sharedkernel.pagination.Paginate;
import tr.com.huseyinaydin.sharedkernel.pagination.PaginationRequest;
import tr.com.huseyinaydin.domain.fixtures.IndividualCustomerTestFixture;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = TestPersistenceConfig.class)
@Transactional
@Rollback(true)
public class JpaRepositoryIntegrationTest {

    @PersistenceContext
    private EntityManager entityManager;

    private IndividualCustomerJpaRepository repository;

    @BeforeEach
    void setUp() {
        repository = new IndividualCustomerJpaRepository(entityManager);
    }

    @Test
    @DisplayName("getAsync (findById) ile kayıt bulunabilmeli")
    void testFindById() {
        IndividualCustomer customer = IndividualCustomerTestFixture.random();
        entityManager.persist(customer);
        entityManager.flush();

        Optional<IndividualCustomer> found = repository.findById(customer.getId());

        assertThat(found).isPresent();
        assertThat(found.get().getNationalId()).isEqualTo(customer.getNationalId());
    }

    @Test
    @DisplayName("getListAsync (findAll ile sayfalama) doğru çalışmalı")
    void testFindAllWithPagination() {
        for (int i = 0; i < 10; i++) {
            entityManager.persist(IndividualCustomerTestFixture.random());
        }
        entityManager.flush();

        PaginationRequest pagination = new PaginationRequest(0, 5);
        Paginate<IndividualCustomer> result = repository.findAll(null, pagination);

        assertThat(result).isNotNull();
        assertThat(result.getItems()).hasSize(5);
        assertThat(result.getPageIndex()).isEqualTo(0);
        assertThat(result.getPageSize()).isEqualTo(5);
        assertThat(result.getTotalCount()).isGreaterThanOrEqualTo(10);
    }

    @Test
    @DisplayName("softDelete işlemi silinme tarihini set etmeli")
    void testSoftDelete() {
        IndividualCustomer customer = IndividualCustomerTestFixture.random();
        entityManager.persist(customer);
        entityManager.flush();

        repository.delete(customer, false); // soft delete
        entityManager.flush();
        entityManager.clear();

        // JpaRepositoryBase findById ignores deleted items
        Optional<IndividualCustomer> found = repository.findById(customer.getId());
        assertThat(found).isEmpty();
        
        // Native EM find returns the entity, verify deletedDate
        IndividualCustomer dbCustomer = entityManager.find(IndividualCustomer.class, customer.getId());
        assertThat(dbCustomer.getDeletedDate()).isNotNull();
    }

    @Test
    @DisplayName("withDeleted=false olduğunda silinmiş kayıtlar gelmemeli")
    void testFindAllWithoutDeleted() {
        IndividualCustomer activeCustomer = IndividualCustomerTestFixture.random();
        IndividualCustomer deletedCustomer = IndividualCustomerTestFixture.random();
        
        entityManager.persist(activeCustomer);
        entityManager.persist(deletedCustomer);
        entityManager.flush();

        repository.delete(deletedCustomer, false); // soft delete
        entityManager.flush();

        Paginate<IndividualCustomer> result = repository.findAll(null, new PaginationRequest(0, 100), false);
        
        assertThat(result.getItems()).extracting("id").contains(activeCustomer.getId());
        assertThat(result.getItems()).extracting("id").doesNotContain(deletedCustomer.getId());
    }

    @Test
    @DisplayName("withDeleted=true olduğunda silinmiş kayıtlar gelmeli")
    void testFindAllWithDeleted() {
        IndividualCustomer deletedCustomer = IndividualCustomerTestFixture.random();
        entityManager.persist(deletedCustomer);
        entityManager.flush();

        repository.delete(deletedCustomer, false); // soft delete
        entityManager.flush();

        Paginate<IndividualCustomer> result = repository.findAll(null, new PaginationRequest(0, 100), true);
        
        assertThat(result.getItems()).extracting("id").contains(deletedCustomer.getId());
    }

    @Test
    @DisplayName("Specification tabanlı sorgu doğru çalışmalı")
    void testSpecificationQuery() {
        IndividualCustomer customer1 = IndividualCustomerTestFixture.random();
        customer1.setFirstName("Huseyin");
        
        IndividualCustomer customer2 = IndividualCustomerTestFixture.random();
        customer2.setFirstName("Ali");
        
        entityManager.persist(customer1);
        entityManager.persist(customer2);
        entityManager.flush();

        Specification<IndividualCustomer> spec = (root, query, cb) -> cb.equal(root.get("firstName"), "Huseyin");

        Paginate<IndividualCustomer> result = repository.findAll(spec, new PaginationRequest(0, 10));

        assertThat(result.getItems()).hasSize(1);
        assertThat(result.getItems().get(0).getFirstName()).isEqualTo("Huseyin");
    }

    @Test
    @DisplayName("National ID (Ulusal Kimlik) ile arama doğru çalışmalı")
    void testFindByNationalId() {
        IndividualCustomer customer = IndividualCustomerTestFixture.random();
        entityManager.persist(customer);
        entityManager.flush();

        Optional<IndividualCustomer> found = repository.findByNationalId(customer.getNationalId());

        assertThat(found).isPresent();
        assertThat(found.get().getNationalId()).isEqualTo(customer.getNationalId());
    }
}
