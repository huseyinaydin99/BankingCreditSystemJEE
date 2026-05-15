package tr.com.huseyinaydin.domain.valueobjects;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Objects;

public final class Money {

    private final BigDecimal amount;
    private final String currency;

    public Money(BigDecimal amount, String currency) {
        if (amount == null) {
            throw new IllegalArgumentException("Tutar null olamaz");
        }
        if (amount.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Tutar negatif olamaz");
        }
        if (currency == null || currency.isBlank()) {
            throw new IllegalArgumentException("Para birimi boş olamaz");
        }
        this.amount = amount.setScale(2, RoundingMode.HALF_UP);
        this.currency = currency.toUpperCase();
    }

    public static Money of(BigDecimal amount, String currency) {
        return new Money(amount, currency);
    }

    public static Money of(double amount, String currency) {
        return new Money(BigDecimal.valueOf(amount), currency);
    }

    public Money add(Money other) {
        assertSameCurrency(other);
        return new Money(this.amount.add(other.amount), this.currency);
    }

    public Money subtract(Money other) {
        assertSameCurrency(other);
        BigDecimal result = this.amount.subtract(other.amount);
        if (result.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Çıkarma işlemi negatif sonuç üretemez");
        }
        return new Money(result, this.currency);
    }

    public Money multiply(BigDecimal factor) {
        if (factor == null || factor.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Çarpan negatif olamaz");
        }
        return new Money(this.amount.multiply(factor), this.currency);
    }

    public BigDecimal getAmount() { return amount; }
    public String getCurrency() { return currency; }

    private void assertSameCurrency(Money other) {
        if (!this.currency.equals(other.currency)) {
            throw new IllegalArgumentException(
                    "Farklı para birimleriyle işlem yapılamaz: " + this.currency + " ile " + other.currency);
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Money money = (Money) o;
        return Objects.equals(amount, money.amount) && Objects.equals(currency, money.currency);
    }

    @Override
    public int hashCode() {
        return Objects.hash(amount, currency);
    }

    @Override
    public String toString() {
        return amount.toPlainString() + " " + currency;
    }
}
