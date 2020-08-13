package edu.tdd.example.services;

import edu.tdd.example.domain.Product;
import edu.tdd.example.repositories.ProductRepository;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.util.AssertionErrors;

import java.util.Arrays;
import java.util.Collection;

import static org.mockito.Mockito.*;

@SpringBootTest
@ExtendWith({SpringExtension.class})
public class ProductServiceTest {

    @Autowired
    private ProductService productService;

    @MockBean
    private ProductRepository productRepository;

    @Test
    @DisplayName("Test find product with id successfully")
    public void testFindProductById(){

        Product mockProduct = new Product();
        mockProduct.setId(1);
        mockProduct.setVersion(1);
        mockProduct.setName("New Product");
        mockProduct.setDescription("New Product Description");
        mockProduct.setQuantity(8);

        doReturn(mockProduct).when(productRepository).findProductById(1);

        Product foundProduct = productService.findById(1);

        Assertions.assertNotNull(foundProduct);
        Assertions.assertSame("New Product",foundProduct.getName());
    }

    @Test
    @DisplayName("Test fail to find product with id")
    public void testFailToFindProductById(){

        doReturn(null).when(productRepository).findProductById(1);

        Product foundProduct = productService.findById(1);

        Assertions.assertNull(foundProduct);
    }

    @Test
    @DisplayName("Test Find All product")
    public void testFindAllProduct(){

        Product firstProduct = new Product();
        firstProduct.setId(1);
        firstProduct.setVersion(1);
        firstProduct.setName("1st Product");
        firstProduct.setDescription("Product Description");
        firstProduct.setQuantity(8);

        Product secondProduct = new Product();
        secondProduct.setId(2);
        secondProduct.setVersion(1);
        secondProduct.setName("2nd Product");
        secondProduct.setDescription("Product Description");
        secondProduct.setQuantity(10);

        doReturn(Arrays.asList(firstProduct,secondProduct)).when(productRepository).findAll();

        Iterable<Product> allProducts = productService.findAll();

        Assertions.assertEquals(2,((Collection<?>) allProducts).size());
    }

    @Test
    @DisplayName("Test save product successfully")
    public void testSaveProductSuccessfully(){

        Product mockProduct = new Product();
        mockProduct.setVersion(1);
        mockProduct.setQuantity(8);
        mockProduct.setName("New Product");
        mockProduct.setDescription("New Product Description");

        doReturn(mockProduct).when(productRepository).save(any());

        Product savedProduct = productService.save(mockProduct);

        AssertionErrors.assertNotNull("Product should not be null",savedProduct);
        Assertions.assertSame("New Product",savedProduct.getName());
        Assertions.assertSame(1,savedProduct.getVersion());
    }

    @Test
    @DisplayName("Test Updating product successfully")
    public void testUpdatingProductSuccessfully(){

        Product existingProduct = new Product();
        existingProduct.setId(1);
        existingProduct.setVersion(1);
        existingProduct.setName("Product");
        existingProduct.setDescription("Product Description");
        existingProduct.setQuantity(8);

        Product updatedProduct = new Product();
        updatedProduct.setId(1);
        updatedProduct.setVersion(2);
        updatedProduct.setName("New Product");
        updatedProduct.setDescription("Product Description");
        updatedProduct.setQuantity(10);

        doReturn(existingProduct).when(productRepository).findProductById(1);
        doReturn(updatedProduct).when(productRepository).save(updatedProduct);

        Product updateProduct = productService.save(updatedProduct);

        Assertions.assertEquals("New Product",updateProduct.getName());
    }

    @Test
    @DisplayName("Test fail to update an existing product")
    public void testFailUpdatingProduct(){

        Product mockProduct = new Product();
        mockProduct.setId(1);
        mockProduct.setVersion(1);
        mockProduct.setName("Product");
        mockProduct.setDescription("Product Description");
        mockProduct.setQuantity(8);

        doReturn(null).when(productRepository).findProductById(1);

        Product updatedProduct = productService.save(mockProduct);

        AssertionErrors.assertNull("Product should be null",updatedProduct);
    }
}
