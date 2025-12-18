package org.yearup.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import org.yearup.data.ProductDao;
import org.yearup.models.Product;

import java.math.BigDecimal;
import java.util.List;

@RestController
// base endpoint for everything product-related
// example: http://localhost:8080/products
@RequestMapping("products")
// allow frontend apps to hit these endpoints
@CrossOrigin
public class ProductsController {

    private ProductDao productDao;

    @Autowired
    public ProductsController(ProductDao productDao) {
        this.productDao = productDao;
    }

    // supports optional query params for searching/filtering
    @GetMapping("")
    @PreAuthorize("permitAll()")
    public List<Product> search(
            // category filter
            @RequestParam(name = "cat", required = false) Integer categoryId,

            // minimum price filter
            @RequestParam(name = "minPrice", required = false) BigDecimal minPrice,

            // maximum price filter
            @RequestParam(name = "maxPrice", required = false) BigDecimal maxPrice,

            // optional subcategory filter
            @RequestParam(name = "subCategory", required = false) String subCategory) {
        try {
            // delegate all filtering logic to the dao
            return productDao.search(categoryId, minPrice, maxPrice, subCategory);
        } catch (Exception ex) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Oops... our bad.");
        }
    }

    // returns a single product by id
    @GetMapping("{id}")
    @PreAuthorize("permitAll()")
    public Product getById(@PathVariable int id) {
        try {
            // fetch product from db
            var product = productDao.getById(id);

            // if product doesn’t exist, return 404
            if (product == null) throw new ResponseStatusException(HttpStatus.NOT_FOUND);

            return product;
        } catch (Exception ex) {

            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Oops... our bad.");
        }
    }

    // creates a new product
    @PostMapping()
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public Product addProduct(@RequestBody Product product) {
        try {
            // insert product into the database
            return productDao.create(product);
        } catch (Exception ex) {
            // generic error response
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Oops... our bad.");
        }
    }

    // updates an existing product
    @PutMapping("{id}")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public void updateProduct(@PathVariable int id, @RequestBody Product product) {
        try {
            // update the product instead of creating a new one
            //  this was the bug before (create() instead of update())
            productDao.update(id, product);
        } catch (Exception ex) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Oops... our bad.");
        }
    }

    // deletes a product
    @DeleteMapping("{id}")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public void deleteProduct(@PathVariable int id) {
        try {
            // check if product exists before deleting
            var product = productDao.getById(id);

            if (product == null) throw new ResponseStatusException(HttpStatus.NOT_FOUND);

            // delete product from db
            productDao.delete(id);
        } catch (Exception ex) {

            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Oops... our bad.");
        }
    }
}