package edu.tdd.example.ut.repositories;

import com.fasterxml.jackson.databind.ObjectMapper;
import edu.tdd.example.domain.Product;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.Arrays;

@SpringBootTest
@ExtendWith({SpringExtension.class})
public class ProductRepositoryTests {

    @Autowired
    private ProductRepository productRepository;

    private static final File DATA_JSON = Paths.get("src","test","resources","products.json").toFile();

    @BeforeEach
    public void setup() throws IOException {
        // Deserialize products from JSON file to array
        Product[] products = new ObjectMapper().readValue(DATA_JSON,Product[].class);

        // save each product to database
        Arrays.stream(products).forEach(productRepository::save);
    }

    @AfterEach
    public void cleanup() {
        // cleanup database after each test
        productRepository.deleteAll();
    }


    @Test
    @DisplayName("Test product with id retrieved successfully")
    public void testProductWithIdRetrievedSuccessfully() {
        // given two products in the database

        // when
        Product retrievedProduct = productRepository.findProductById(1);
        // then
        Assertions.assertNotNull(retrievedProduct,"Product with id 1 should exist");
        Assertions.assertEquals("First Product",retrievedProduct.getName());
    }

    @Test
    @DisplayName("Test product not found with non-existing id")
    public void testProductNotFoundForNonExistingId() {
        // given two products in the database

        // when
        Product retrievedProduct = productRepository.findProductById(100);

        // then
        Assertions.assertNull(retrievedProduct,"Product with id 100 should not exist");
    }

    @Test
    @DisplayName("Test product saved successfully")
    public void testProductSavedSuccessfully() {
        // Prepare mock product
        Product newProduct = new Product();
        newProduct.setId(1);
        newProduct.setName("New Product");
        newProduct.setDescription("New Product Description");
        newProduct.setQuantity(8);

        // when
        Product savedProduct = productRepository.save(newProduct);

        // then
        Assertions.assertNotNull(savedProduct,"Product should be saved");
        Assertions.assertNotNull(savedProduct.getId(),"Product should have an id when saved");
        Assertions.assertEquals(savedProduct.getName(),newProduct.getName());
    }

    @Test
    @DisplayName("Test product updated successfully")
    public void testProductUpdatedSuccessfully() {
        // Prepare mock product
        Product productToUpdate = new Product();
        productToUpdate.setId(1);
        productToUpdate.setName("Updated Product");
        productToUpdate.setDescription("New Product Description");
        productToUpdate.setQuantity(80);
        productToUpdate.setVersion(2);

        // when
        Product updatedProduct = productRepository.save(productToUpdate);

        // then
        Assertions.assertEquals(updatedProduct.getName(),productToUpdate.getName());
        Assertions.assertEquals(2,productToUpdate.getVersion());
        Assertions.assertEquals(80,productToUpdate.getQuantity());
    }

    @Test
    @DisplayName("Test product deleted successfully")
    public void testProductDeletedSuccessfully() {
        // given two products in the database

        // when
        productRepository.deleteById(2);

        // then
        Assertions.assertEquals(1L,productRepository.count());
    }
}
