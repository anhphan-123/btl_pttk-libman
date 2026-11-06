package libman.util;

import java.math.BigDecimal;

public final class ValidationUtil8 {
    /** Giá trị lớn nhất mà cột MySQL DECIMAL(12,2) có thể lưu. */
    public static final BigDecimal MAX_MONEY = new BigDecimal("9999999999.99");

    private ValidationUtil8() {
    }

    public static boolean hasText(String value) {
        return value != null && !value.trim().isEmpty();
    }

    public static boolean isPositiveQuantity(Integer quantity) {
        return quantity != null && quantity > 0;
    }

    public static boolean isNonNegativePrice(BigDecimal price) {
        return price != null && price.signum() >= 0;
    }

    public static boolean isValidMoney(BigDecimal amount) {
        return isNonNegativePrice(amount)
                && amount.scale() <= 2
                && amount.compareTo(MAX_MONEY) <= 0;
    }
}
