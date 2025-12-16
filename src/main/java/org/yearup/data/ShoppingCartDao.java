package org.yearup.data;

import org.yearup.models.ShoppingCart;

public interface ShoppingCartDao {
    ShoppingCart getByUserId(int userId);
    // add additional method signatures here

    void addProduct(int userId, int productId);

    void updateQuantity(int userId, int productId, int quantity);

    boolean exists(int userId, int productId);

    int getQuantity(int userId, int productId);

    void clearCart(int userId);
}