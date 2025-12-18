package org.yearup.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import org.yearup.data.CategoryDao;
import org.yearup.data.ProductDao;
import org.yearup.models.Category;
import org.yearup.models.Product;

import java.util.List;

// add the annotations to make this a REST controller
// this controller handles everything under /categories
// example base url: http://localhost:8080/categories
@RestController
@RequestMapping("categories")
// allow frontend apps to call these endpoints
@CrossOrigin
public class CategoriesController {

    // dao for category table
    private CategoryDao categoryDao;

    // dao for product table so I can use it when fetching products by category
    private ProductDao productDao;

    // constructor injection so spring wires the daos automatically
    @Autowired
    public CategoriesController(CategoryDao categoryDao, ProductDao productsDao) {
        this.categoryDao = categoryDao;
        this.productDao = productsDao;
    }

    // returns all categories
    @GetMapping("")
    @PreAuthorize("permitAll()")
    public List<Category> getAll() {
        // fetch and return all categories from db
        try {
            return categoryDao.getAllCategories();
        } catch (Exception ex) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Something went wrong with the system");
        }
    }


    // returns a single category by id
    @GetMapping("{id}")
    @PreAuthorize("permitAll()")
    public Category getById(@PathVariable int id) {
        // fetch category by id
        try {
            var category = categoryDao.getById(id);

            // if category doesn’t exist return 404
            if (category == null) {
                throw new ResponseStatusException(HttpStatus.NOT_FOUND);
            }

            return category;
        } catch (Exception ex) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Something went wrong internally");
        }
    }

    // GET /categories/{categoryId}/products
    // example:
    // https://localhost:8080/categories/1/products
    // returns all products for a given category
    @GetMapping("{categoryId}/products")
    public List<Product> getProductsById(@PathVariable int categoryId) {
        // fetch all products that belong to this category
        try {
            return productDao.listByCategoryId(categoryId);
        } catch (Exception ex) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Something went wrong internally");
        }
    }

    // POST /categories
    // creates a new category
    // should be admin only
    @PostMapping("")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public Category addCategory(@RequestBody Category category) {
        // insert category into the database
        try {
            return categoryDao.create(category);
        } catch (Exception ex) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Oops... our bad.");
        }
    }

    // PUT /categories/{id}
    // updates an existing category
    // should be admin only
    @PutMapping("{id}")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public void updateCategory(@PathVariable int id, @RequestBody Category category) {
        // update category fields by id
        try {
            categoryDao.update(id, category);
        } catch (Exception ex) {
            // something went wrong while updating
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Oops... our bad.");
        }
    }

    // DELETE /categories/{id}
    // deletes a category
    // admin only allowed
    @DeleteMapping("{id}")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public void deleteCategory(@PathVariable int id) {
        // delete category if it exists
        try {
            var product = categoryDao.getById(id);

            if (product == null) throw new ResponseStatusException(HttpStatus.NOT_FOUND);

            // delete category from db
            categoryDao.delete(id);
        } catch (Exception ex) {

            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Oops... our bad.");
        }
    }
}