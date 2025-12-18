package org.yearup.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.yearup.data.*;
import org.yearup.models.*;

import java.security.Principal;
import java.util.Collection;
import java.util.List;

@RestController
@RequestMapping("/orders")
public class OrdersController
{
    private final OrderDao orderDao;
    private final OrderLineItemDao orderLineItemDao;
    private final ShoppingCartDao shoppingCartDao;
    private final UserDao userDao;
    private final ProductDao productDao;


    @Autowired
    public OrdersController(OrderDao orderDao,
                            OrderLineItemDao orderLineItemDao,
                            ShoppingCartDao shoppingCartDao,
                            UserDao userDao,
                            ProductDao productDao)
    {
        this.orderDao = orderDao;
        this.orderLineItemDao = orderLineItemDao;
        this.shoppingCartDao = shoppingCartDao;
        this.userDao = userDao;
        this.productDao = productDao;
    }

    // POST /orders
    @PostMapping
    public void checkout(Principal principal)
    {
        if (principal == null)
        {
            throw new RuntimeException("User not authenticated");
        }

        // Get logged-in user
        int userId = userDao.getIdByUsername(principal.getName());

        // Get shopping cart (correct way)
        ShoppingCart cart = shoppingCartDao.getByUserId(userId);
        Collection<ShoppingCartItem> cartItems = cart.getItems().values();



        if (cartItems.isEmpty())
        {
            throw new IllegalStateException("Shopping cart is empty");

        }

        // Create order
        Order order = orderDao.create(new Order(userId));

        // Convert cart items → order line items
        for (ShoppingCartItem cartItem : cartItems)
        {
            if (cartItem.getQuantity() <= 0) {
                continue;
            }

            Product product = productDao.getById(cartItem.getProductId());
            if (product == null) {
                throw new RuntimeException("Product not found: " + cartItem.getProductId());
            }

            OrderLineItem item = new OrderLineItem(
                    order.getOrderId(),
                    cartItem.getProductId(),
                    cartItem.getQuantity(),
                    product.getPrice()
            );

            orderLineItemDao.create(item);
        }


        // Clear cart
        shoppingCartDao.clearCart(userId);
    }

}