package edu.tdd.example.repositories;

import edu.tdd.example.domain.Product;
import org.springframework.data.repository.CrudRepository;

public interface ProductRepository extends CrudRepository<Product,Integer> {
    Product findProductById(Integer id);
}
