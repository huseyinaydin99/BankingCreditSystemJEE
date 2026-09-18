package tr.com.huseyinaydin.sharedkernel.pagination;

import java.util.Collections;
import java.util.List;

public class Paginate<T> {

    private final List<T> items;
    private final int pageIndex;
    private final int pageSize;
    private final long totalCount;
    private final String nextCursor; // Keyset pagination için eklendi

    public Paginate(List<T> items, int pageIndex, int pageSize, long totalCount) {
        this.items = Collections.unmodifiableList(items != null ? items : Collections.emptyList());
        this.pageIndex = pageIndex;
        this.pageSize = pageSize;
        this.totalCount = totalCount;
        this.nextCursor = null;
    }

    public Paginate(List<T> items, String nextCursor, int pageSize) {
        this.items = Collections.unmodifiableList(items != null ? items : Collections.emptyList());
        this.pageIndex = 0;
        this.pageSize = pageSize;
        this.totalCount = 0; // Keyset'te count hesaplanmaz
        this.nextCursor = nextCursor;
    }

    public List<T> getItems() { return items; }
    public int getPageIndex() { return pageIndex; }
    public int getPageSize() { return pageSize; }
    public long getTotalCount() { return totalCount; }
    public String getNextCursor() { return nextCursor; }

    public int getTotalPages() {
        if (pageSize <= 0 || totalCount <= 0) return 0;
        return (int) Math.ceil((double) totalCount / pageSize);
    }

    public boolean hasNextPage() { 
        if (nextCursor != null) return !items.isEmpty() && items.size() == pageSize;
        return pageIndex < getTotalPages() - 1; 
    }
    public boolean hasPreviousPage() { return pageIndex > 0; }
    public boolean isFirstPage() { return pageIndex == 0; }
    public boolean isLastPage() { 
        if (nextCursor != null) return items.size() < pageSize;
        return pageIndex >= getTotalPages() - 1; 
    }
}
