package tr.com.huseyinaydin.sharedkernel.pagination;

import java.util.Collections;
import java.util.List;

public class Paginate<T> {

    private final List<T> items;
    private final int pageIndex;
    private final int pageSize;
    private final long totalCount;

    public Paginate(List<T> items, int pageIndex, int pageSize, long totalCount) {
        this.items = Collections.unmodifiableList(items);
        this.pageIndex = pageIndex;
        this.pageSize = pageSize;
        this.totalCount = totalCount;
    }

    public List<T> getItems() { return items; }
    public int getPageIndex() { return pageIndex; }
    public int getPageSize() { return pageSize; }
    public long getTotalCount() { return totalCount; }

    public int getTotalPages() {
        if (pageSize <= 0) return 0;
        return (int) Math.ceil((double) totalCount / pageSize);
    }

    public boolean hasNextPage() { return pageIndex < getTotalPages() - 1; }
    public boolean hasPreviousPage() { return pageIndex > 0; }
    public boolean isFirstPage() { return pageIndex == 0; }
    public boolean isLastPage() { return pageIndex >= getTotalPages() - 1; }
}
