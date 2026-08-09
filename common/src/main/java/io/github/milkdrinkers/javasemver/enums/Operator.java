package io.github.milkdrinkers.javasemver.enums;

/**
 * Represents a comparison operator used by a single semver range comparator e.g. the {@code >=} in {@code >=1.2.3}.
 */
public enum Operator {
    /**
     * Greater than ({@code >}).
     */
    GT(">"),

    /**
     * Greater than or equal to ({@code >=}).
     */
    GTE(">="),

    /**
     * Less than ({@code <}).
     */
    LT("<"),

    /**
     * Less than or equal to ({@code <=}).
     */
    LTE("<="),

    /**
     * Equal to ({@code =}).
     */
    EQ("=");

    private final String symbol;

    Operator(String symbol) {
        this.symbol = symbol;
    }

    /**
     * Gets the textual symbol representing this operator e.g. {@code ">="}.
     *
     * @return the symbol
     */
    public String getSymbol() {
        return symbol;
    }

    /**
     * Finds the operator matching the given symbol.
     *
     * @param symbol the symbol to look up e.g. {@code ">="}
     * @return the matching operator, or {@code null} if no operator has that symbol
     */
    public static Operator fromSymbol(String symbol) {
        for (Operator operator : values()) {
            if (operator.symbol.equals(symbol))
                return operator;
        }

        return null;
    }
}
