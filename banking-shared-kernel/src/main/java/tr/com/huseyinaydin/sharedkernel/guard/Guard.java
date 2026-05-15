package tr.com.huseyinaydin.sharedkernel.guard;

import tr.com.huseyinaydin.sharedkernel.exception.ValidationError;
import tr.com.huseyinaydin.sharedkernel.exception.ValidationException;

import java.math.BigDecimal;
import java.util.Collection;

public final class Guard {

    private Guard() {}

    public static <T> T notNull(T value, String paramName) {
        if (value == null) {
            throw new NullPointerException(paramName + " null olamaz");
        }
        return value;
    }

    public static String notBlank(String value, String paramName) {
        if (value == null || value.isBlank()) {
            throw new ValidationException(
                    new ValidationError(paramName, paramName + " boş olamaz", value));
        }
        return value;
    }

    public static <C extends Collection<?>> C notEmpty(C collection, String paramName) {
        if (collection == null || collection.isEmpty()) {
            throw new ValidationException(
                    new ValidationError(paramName, paramName + " boş olamaz", collection));
        }
        return collection;
    }

    public static BigDecimal inRange(BigDecimal value, BigDecimal min, BigDecimal max, String paramName) {
        notNull(value, paramName);
        if (value.compareTo(min) < 0 || value.compareTo(max) > 0) {
            throw new ValidationException(new ValidationError(
                    paramName,
                    paramName + " " + min + " ile " + max + " arasında olmalıdır",
                    value));
        }
        return value;
    }
}
