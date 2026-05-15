package tr.com.huseyinaydin.sharedkernel.pagination;

import tr.com.huseyinaydin.sharedkernel.exception.ValidationError;
import tr.com.huseyinaydin.sharedkernel.exception.ValidationException;

import java.util.ArrayList;
import java.util.List;

public class PaginationRequest {

    public static final int DEFAULT_PAGE_SIZE = 10;
    public static final int MAX_PAGE_SIZE = 100;

    private int pageIndex;
    private int pageSize;

    public PaginationRequest() {
        this.pageIndex = 0;
        this.pageSize = DEFAULT_PAGE_SIZE;
    }

    public PaginationRequest(int pageIndex, int pageSize) {
        this.pageIndex = pageIndex;
        this.pageSize = pageSize;
    }

    public void validate() {
        List<ValidationError> errors = new ArrayList<>();
        if (pageIndex < 0) {
            errors.add(new ValidationError("pageIndex", "Sayfa numarası negatif olamaz", pageIndex));
        }
        if (pageSize < 1) {
            errors.add(new ValidationError("pageSize", "Sayfa boyutu en az 1 olmalıdır", pageSize));
        }
        if (pageSize > MAX_PAGE_SIZE) {
            errors.add(new ValidationError("pageSize",
                    "Sayfa boyutu en fazla " + MAX_PAGE_SIZE + " olabilir", pageSize));
        }
        if (!errors.isEmpty()) {
            throw new ValidationException(errors);
        }
    }

    public int getPageIndex() { return pageIndex; }
    public void setPageIndex(int pageIndex) { this.pageIndex = pageIndex; }

    public int getPageSize() { return pageSize; }
    public void setPageSize(int pageSize) { this.pageSize = pageSize; }
}
