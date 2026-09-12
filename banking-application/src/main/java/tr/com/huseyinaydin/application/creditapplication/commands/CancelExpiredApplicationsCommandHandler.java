package tr.com.huseyinaydin.application.creditapplication.commands;

import org.springframework.stereotype.Component;
import tr.com.huseyinaydin.sharedkernel.messaging.ICommandHandler;
import tr.com.huseyinaydin.application.creditapplication.rules.CreditApplicationBusinessRules;
import tr.com.huseyinaydin.domain.creditapplication.CreditApplication;
import tr.com.huseyinaydin.domain.enums.CreditApplicationStatus;
import tr.com.huseyinaydin.domain.repositories.ICreditApplicationRepository;
import tr.com.huseyinaydin.domain.repositories.Specification;

import java.util.List;

@Component
public class CancelExpiredApplicationsCommandHandler implements ICommandHandler<CancelExpiredApplicationsCommand, Integer> {

    private final ICreditApplicationRepository creditApplicationRepository;
    private final CreditApplicationBusinessRules businessRules;

    public CancelExpiredApplicationsCommandHandler(ICreditApplicationRepository creditApplicationRepository,
                                                   CreditApplicationBusinessRules businessRules) {
        this.creditApplicationRepository = creditApplicationRepository;
        this.businessRules = businessRules;
    }

    @Override
    public Integer handle(CancelExpiredApplicationsCommand request) {
        Specification<CreditApplication> spec = (root, query, cb) -> cb.and(
                cb.equal(root.get("status"), CreditApplicationStatus.PENDING),
                cb.lessThan(root.get("createdDate"), request.getOlderThan())
        );

        List<CreditApplication> expiredApps = creditApplicationRepository.findAll(spec, 
            new tr.com.huseyinaydin.sharedkernel.pagination.PaginationRequest(0, 1000)).getItems();

        int count = 0;
        for (CreditApplication app : expiredApps) {
            businessRules.canBeExpired(app);
            app.cancel();
            creditApplicationRepository.update(app);
            count++;
        }
        return count;
    }
}
