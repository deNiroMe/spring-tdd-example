package edu.tdd.example.services;

import edu.tdd.example.domain.Product;
import edu.tdd.example.repositories.ProductRepository;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@AllArgsConstructor
public class ProductService {

    private ProductRepository productRepository;

    public Product save(Product product){
        log.debug("Saving new product with name: {}",product.getName());
        product.setVersion(1);
        return productRepository.save(product);
    }

    public Product update(Product product){
        log.debug("Updating new product with id: {}",product.getId());
        Product existingProduct = productRepository.findProductById(product.getId());

        if(existingProduct != null) {
            existingProduct.setQuantity(product.getQuantity());
            existingProduct.setDescription(product.getDescription());
            existingProduct.setName(product.getName());
            existingProduct = productRepository.save(existingProduct);
        } else {
            log.error("Product with id {} could not be updated!",product.getId());
        }
        return existingProduct;
    }

    public Product findById(Integer id){
        log.debug("Retrieving product with id: {}", id);

        return productRepository.findProductById(id);
    }

    public void delete(Integer id){
        log.debug("deleting product with id: {}", id);

        Product existingProduct = productRepository.findProductById(id);

        if(existingProduct != null) {
            productRepository.deleteById(id);
        } else {
            log.error("Product with id {} could not be found!",id);
        }
    }

    public Iterable<Product> findAll(){ return productRepository.findAll(); }
}
