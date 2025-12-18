package org.yearup.controllers;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import org.yearup.data.ShoppingCartDao;
import org.yearup.data.UserDao;
import org.yearup.models.ShoppingCart;
import org.yearup.models.ShoppingCartItem;
import org.yearup.models.User;

import java.security.Principal;

// convert this class to a REST controller
// only logged in users should have access to these actions
@RestController
// base route for everything in this controller
@RequestMapping("/cart")
// allow frontend to call these endpoints from cross origin
@CrossOrigin
public class ShoppingCartController {

    private ShoppingCartDao shoppingCartDao;
    private UserDao userDao;
//    private ProductDao productDao;

    // constructor injection so spring gives us the daos automatically
    public ShoppingCartController(ShoppingCartDao shoppingCartDao, UserDao userDao) {
        this.shoppingCartDao = shoppingCartDao;
        this.userDao = userDao;
//        this.productDao = productDao;
    }

    // each method in this controller requires a Principal object as a parameter
    // principal tells me who is currently logged in
    @GetMapping
    public ShoppingCart getCart(Principal principal) {
        try {
            // get the username of the currently logged in user
            String userName = principal.getName();

            // look up the full user record from the database
            User user = userDao.getByUserName(userName);

            // grab the user id since carts are tied to user ids
            int userId = user.getId();

            // fetch and return the entire shopping cart for this user
            return shoppingCartDao.getByUserId(userId);
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Oops... our bad.");
        }
    }

    // add a POST method to add a product to the cart
    // example url:
    // https://localhost:8080/cart/products/15
    @PostMapping("/products/{productId}")
    @ResponseStatus(HttpStatus.CREATED)
    public void addProductToCart(@PathVariable int productId, Principal principal) {

        // get the logged in user using the principal
        User user = userDao.getByUserName(principal.getName());

        // extract user id
        int userId = user.getId();

        // check if this product already exists in the cart
        if (shoppingCartDao.exists(userId, productId)) {

            // get current quantity from the cart
            int currentQty = shoppingCartDao.getQuantity(userId, productId);

            // basically repeated post requests will just increment quantity by 1
            shoppingCartDao.updateQuantity(userId, productId, currentQty + 1);
        } else {
            // if product is not in cart yet add it with quantity 1
            shoppingCartDao.addProduct(userId, productId);
        }
    }

    // add a PUT method to update an existing product in the cart
    // example url:
    // https://localhost:8080/cart/products/15
    // body contains a ShoppingCartItem
    // only quantity is allowed to change
    @PutMapping("/products/{productId}")
    public void updateProductInCart(@PathVariable int productId, @RequestBody ShoppingCartItem item, Principal principal) {

        // get the logged-in user
        User user = userDao.getByUserName(principal.getName());

        // get user id
        int userId = user.getId();

        // only update if the product already exists in the cart
        if (shoppingCartDao.exists(userId, productId)) {

            // update quantity to whatever the client sent
            // this does NOT increment, it overwrites
            shoppingCartDao.updateQuantity(userId, productId, item.getQuantity());
        }
        // if product doesn’t exist, we silently do nothing
        // could also throw a 404 but assignment doesn’t require it
    }

    // add a DELETE method to clear all products from the current users cart
    // example url:
    // https://localhost:8080/cart
    @DeleteMapping
    // 204 = no content, since nothing is returned
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void clearCart(Principal principal) {

        // get the logged-in user
        User user = userDao.getByUserName(principal.getName());

        // remove all cart items for this user
        shoppingCartDao.clearCart(user.getId());
    }
}