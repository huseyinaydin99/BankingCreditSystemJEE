package tr.com.huseyinaydin.domain.fixtures;

import tr.com.huseyinaydin.domain.customer.CorporateCustomer;

import java.util.UUID;

public class CorporateCustomerTestFixture {

    public static CorporateCustomer createValidCustomer() {
        return createValidCustomer(UUID.randomUUID(), "1111111111");
    }

    public static CorporateCustomer createValidCustomer(UUID id, String taxNumber) {
        CorporateCustomer customer = new CorporateCustomer(
                "Örnek Şirket A.Ş.",
                taxNumber,
                "info@orneksirket.com"
        );

        customer.updateCompanyDetails(
                "123456-5",
                "Marmara Kurumlar"
        );

        customer.updateContactInfo(
                "2125551234",
                "info@orneksirket.com",
                "Plaza İş Merkezi No:1 Kat:10"
        );

        return customer;
    }
}
