package com.periplus.utils;

import java.util.List;

public final class CartTestData {
    public static final Product ATOMIC_HABITS = new Product("atomic habits", "Atomic Habits");
    public static final Product PYTHON = new Product("python", "Python");
    public static final Product DATA_SCIENCE = new Product("data science", "Data");

    private CartTestData() {
    }

    public static List<Product> multipleProducts() {
        return List.of(ATOMIC_HABITS, PYTHON, DATA_SCIENCE);
    }

    public record Product(String searchTerm, String expectedTitle) {
    }
}
