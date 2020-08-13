package edu.tdd.example.controllers;

import edu.tdd.example.domain.Product;
import edu.tdd.example.services.ProductService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.net.URISyntaxException;

@Slf4j
@RestController
@AllArgsConstructor
public class ProductController {

    private ProductService productService;

    /**
     * Gets all the products in repository
     * @return Iterable list of all products
     */
    @GetMapping("/products")
    public Iterable<Product> getAllProducts() {
        return productService.findAll();
    }

    /**
     * Gets the product with specified ID
     * @param id ID of the product to get
     * @return ResponseEntity with the found product
     *          or NOT_FOUND if no product found
     */
    @GetMapping("/products/{id}")
    public ResponseEntity<?> getProduct(@PathVariable Integer id) {

        Product product = productService.findById(id);

        if(product != null) {
            try {
                return ResponseEntity
                           .ok()
                           .eTag(Integer.toString(id))
                           .location(new URI("/products/"+id))
                           .body(product);
            } catch (URISyntaxException e){
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
            }
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * Saves a new Product
     * @param product to save
     * @return ResponseEntity with the saved product
     */
    @PostMapping("/products")
    public ResponseEntity<?> saveProduct(@RequestBody Product product) {
        log.debug("adding new product with name : {}",product.getName());

        Product createdProduct = productService.save(product);

        try {
            return ResponseEntity
                    .created(new URI("/products/"+createdProduct.getId()))
                    .eTag(Integer.toString(createdProduct.getId()))
                    .body(createdProduct);
        } catch (URISyntaxException e){
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Update an existing Product
     * @param product to update
     * @param ifMatch eTag of the product to update
     * @return ResponseEntity with the updated product
     *          or CONFLICT id eTag versions do not match
     */
    @PutMapping("/products/{id}")
    public ResponseEntity<?> updateProduct(@PathVariable Integer id,
                                           @RequestBody Product product,
                                           @RequestHeader("If-Match") Integer ifMatch) {

        Product existingProduct = productService.findById(id);

        if(existingProduct != null) {

            if(!existingProduct.getVersion().equals(ifMatch)) {
                return ResponseEntity.status(HttpStatus.CONFLICT).build();
            } else {
                log.debug("Updating product with name : {}",existingProduct.getName());

                existingProduct.setVersion(existingProduct.getVersion()+1);
                existingProduct.setName(product.getName());
                existingProduct.setDescription(product.getDescription());
                existingProduct.setQuantity(product.getQuantity());

                try {

                    existingProduct = productService.save(existingProduct);

                    return ResponseEntity
                            .ok()
                            .eTag(Integer.toString(existingProduct.getId()))
                            .location(new URI("/products/"+existingProduct.getId()))
                            .body(existingProduct);
                } catch (URISyntaxException e){
                    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
                }
            }
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * Deletes a product with given id
     * @param id product id to delete
     * @return ResponseEntity with the http status
     */
    @DeleteMapping("/products/{id}")
    public ResponseEntity<?> deleteProduct(@PathVariable Integer id) {
        log.debug("deleting product with id : {}",id);

        Product existingProduct = productService.findById(id);

        if(existingProduct != null){
            productService.delete(id);
            return ResponseEntity.ok().build();
        } else {
            return ResponseEntity.notFound().build();
        }
    }
}

