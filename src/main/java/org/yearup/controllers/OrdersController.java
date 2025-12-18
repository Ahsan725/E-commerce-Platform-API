package org.yearup.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.yearup.data.*;
import org.yearup.models.*;

import java.security.Principal;
import java.util.Collection;

@RestController
// base route for the checkout order api
@RequestMapping("/orders")
// allow frontend access
@CrossOrigin
public class OrdersController {
    // dao for creating and fetching orders
    private final OrderDao orderDao;

    // dao for order_line_items table
    private final OrderLineItemDao orderLineItemDao;

    // dao for reading and clearing shopping cart
    private final ShoppingCartDao shoppingCartDao;

    // dao for user lookups
    private final UserDao userDao;

    // dao for product data (price, existence, etc)
    private final ProductDao productDao;

    @Autowired
    public OrdersController(OrderDao orderDao, OrderLineItemDao orderLineItemDao, ShoppingCartDao shoppingCartDao, UserDao userDao, ProductDao productDao) {
        // assign all daos to class fields
        this.orderDao = orderDao;
        this.orderLineItemDao = orderLineItemDao;
        this.shoppingCartDao = shoppingCartDao;
        this.userDao = userDao;
        this.productDao = productDao;
    }

    // POST /orders
    // this is the "checkout" endpoint
    // no request body needed because everything comes from the user's cart
    @PostMapping
    public void checkout(Principal principal) {
        // user must be logged in
        if (principal == null) {
            throw new RuntimeException("User not authenticated");
        }

        // get the logged in user's id using their username
        int userId = userDao.getIdByUsername(principal.getName());

        // pull the full shopping cart for this user
        ShoppingCart cart = shoppingCartDao.getByUserId(userId);

        // get all cart items as a collection
        Collection<ShoppingCartItem> cartItems = cart.getItems().values();

        // you can’t checkout an empty cart
        if (cartItems.isEmpty()) {
            throw new IllegalStateException("Shopping cart is empty");
        }

        // create a new order record tied to this user
        Order order = orderDao.create(new Order(userId));

        // loop through each cart item and convert it into an order line item
        for (ShoppingCartItem cartItem : cartItems) {
            // skip invalid or zero quantity items just to be safe
            // because it was putting items with 0 quantity in the db
            if (cartItem.getQuantity() <= 0) {
                continue;
            }

            // fetch product info so we can lock in price at checkout time
            Product product = productDao.getById(cartItem.getProductId());

            // if product somehow doesn’t exist throw an exception
            if (product == null) {
                throw new RuntimeException("Product not found: " + cartItem.getProductId());
            }

            // create an order line item from the cart items
            // this is where cart data becomes permanent order data
            OrderLineItem item = new OrderLineItem(order.getOrderId(), // tie line item to the order
                    cartItem.getProductId(), // which product
                    cartItem.getQuantity(), // how many
                    product.getPrice() // price at time of purchase
            );

            // save line item to the database
            orderLineItemDao.create(item);
        }

        // once the order is successfully created we clear the cart to prevent duplicate checkouts
        shoppingCartDao.clearCart(userId);
    }

}