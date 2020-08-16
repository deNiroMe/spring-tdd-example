package edu.tdd.example.ut.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import edu.tdd.example.domain.Product;
import edu.tdd.example.ut.services.ProductService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentMatchers;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;


import java.util.Arrays;
import java.util.List;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import static org.mockito.Mockito.doReturn;

@SpringBootTest(classes = edu.tdd.example.SpringTddExampleApplication.class)
@AutoConfigureMockMvc
@ExtendWith({SpringExtension.class})
public class ProductControllerTest {

    @MockBean
    private ProductService productService;

    @Autowired
    private MockMvc mockMvc;

    @Test
    @DisplayName("Test product found - GET /products/1")
    public void testGetProductByIdFindsProduct() throws Exception {

        Product mockProduct = new Product();

        // Prepare mock product
        mockProduct.setId(1);
        mockProduct.setVersion(1);
        mockProduct.setQuantity(5);
        mockProduct.setName("My Product");
        mockProduct.setDescription("My Product Description");

        // prepare mocked service method
        doReturn(mockProduct).when(productService).findById(mockProduct.getId());

        // perform GET Request
        mockMvc.perform(MockMvcRequestBuilders.get("/products/{id}",mockProduct.getId()))
                // validate 200 OK and JSON response type is received
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))

                //validate response headers
                .andExpect(header().string(HttpHeaders.ETAG,"\"1\""))
                .andExpect(header().string(HttpHeaders.LOCATION,"/products/1"))

                // validate response body
                .andExpect(jsonPath("$.id",is(1)))
                .andExpect(jsonPath("$.name",is("My Product")))
                .andExpect(jsonPath("$.description",is("My Product Description")))
                .andExpect(jsonPath("$.quantity",is(5)))
                .andExpect(jsonPath("$.version",is(1)));
    }

    @Test
    @DisplayName("Test all products found - GET /products")
    public void testAllProductsFound() throws Exception {

        // Prepare mock product
        Product firstProduct = new Product(1,"1st product","1st product description",8,1);
        Product secondProduct = new Product(2,"2st product","2st product description",10,1);


        List<Product> products = Arrays.asList(firstProduct,secondProduct);

        // prepare mocked service method
        doReturn(products).when(productService).findAll();

        // perform GET Request
        mockMvc.perform(MockMvcRequestBuilders.get("/products"))
                // validate 200 OK and JSON response type is received
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))

                // validate response body
                .andExpect(jsonPath("$[0].name",is("1st product")))
                .andExpect(jsonPath("$[1].name",is("2st product")));
    }

    @Test
    @DisplayName("Test add new product - POST /products")
    public void testAddNewProduct() throws Exception {

        // Prepare mock product
        Product newProduct = new Product("New product","New product description",8);
        Product mockProduct = new Product(1,"New product","New product description",8,1);

        // prepare mocked service method
        doReturn(mockProduct).when(productService).save(ArgumentMatchers.any());

        // perform POST Request
        mockMvc.perform(MockMvcRequestBuilders.post("/products")
                            .contentType(MediaType.APPLICATION_JSON_VALUE)
                            .content(new ObjectMapper().writeValueAsString(newProduct))
                        )
                // validate 201 CREATED and JSON response type is received
                .andExpect(status().isCreated())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))

                //validate response headers
                .andExpect(header().string(HttpHeaders.ETAG,"\"1\""))
                .andExpect(header().string(HttpHeaders.LOCATION,"/products/1"))

                // validate response body
                .andExpect(jsonPath("$.id",is(1)))
                .andExpect(jsonPath("$.name",is("New product")))
                .andExpect(jsonPath("$.description",is("New product description")))
                .andExpect(jsonPath("$.quantity",is(8)))
                .andExpect(jsonPath("$.version",is(1)));
    }

    @Test
    @DisplayName("Test update existing - PUT /products/1")
    public void testUpdateExistingProduct() throws Exception {

        // Prepare mock product
        Product productToUpdate = new Product("Updated product","Updated product description",10);
        Product mockProduct = new Product(1,"Mock product","Mock product description",8,1);

        // prepare mocked service methods
        doReturn(mockProduct).when(productService).findById(1);
        doReturn(mockProduct).when(productService).save(ArgumentMatchers.any());

        // perform PUT Request
        mockMvc.perform(MockMvcRequestBuilders.put("/products/{id}",1)
                    .contentType(MediaType.APPLICATION_JSON_VALUE)
                    .header(HttpHeaders.IF_MATCH,1)
                    .content(new ObjectMapper().writeValueAsString(productToUpdate))
                )
                // validate 200 OK and JSON response type is received
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))

                //validate response headers
                .andExpect(header().string(HttpHeaders.ETAG,"\"1\""))
                .andExpect(header().string(HttpHeaders.LOCATION,"/products/1"))

                // validate response body
                .andExpect(jsonPath("$.id",is(1)))
                .andExpect(jsonPath("$.name",is("Updated product")))
                .andExpect(jsonPath("$.description",is("Updated product description")))
                .andExpect(jsonPath("$.quantity",is(10)))
                .andExpect(jsonPath("$.version",is(2)));
    }

    @Test
    @DisplayName("Test version mismatch while updating existing product - PUT /products/1")
    public void testVersionMismatchWhileUpdating() throws Exception {

        // Prepare mock product
        Product productToUpdate = new Product("Updated product","Updated product description",10);
        Product mockProduct = new Product(1,"Mock product","Mock product description",8,2);

        // prepare mocked service method
        doReturn(mockProduct).when(productService).findById(1);

        // perform PUT Request
        mockMvc.perform(MockMvcRequestBuilders.put("/products/{id}",1)
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .header(HttpHeaders.IF_MATCH,1)
                .content(new ObjectMapper().writeValueAsString(productToUpdate))
                )
                // validate 409 CONFLICT received
                .andExpect(status().isConflict());
    }

    @Test
    @DisplayName("Test product not found while updating existing product - PUT /products/1")
    public void testProductNotFoundWhileUpdating() throws Exception {

        // Prepare mock product
        Product productToUpdate = new Product("Updated product","Updated product description",10);

        // prepare mocked service method
        doReturn(null).when(productService).findById(1);

        // perform PUT Request
        mockMvc.perform(MockMvcRequestBuilders.put("/products/{id}",1)
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .header(HttpHeaders.IF_MATCH,1)
                .content(new ObjectMapper().writeValueAsString(productToUpdate))
        )
                // validate 404 NOT_FOUND received
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("Test delete a product successfully - DELETE /products/1")
    public void testDeleteProductSuccessfully() throws Exception {

        // Prepare mock product
        Product existingProduct = new Product(1,"Mock product","Mock product description",8,2);

        // prepare mocked service method
        doReturn(existingProduct).when(productService).findById(1);

        // perform DELETE Request
        mockMvc.perform(MockMvcRequestBuilders.delete("/products/{id}",1))
                // validate 200 OK received
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("Test fail to delete a non existing product - DELETE /products/1")
    public void testFailureToDeleteANonExistingProduct() throws Exception {

        // prepare mocked service method
        doReturn(null).when(productService).findById(1);

        // perform DELETE Request
        mockMvc.perform(MockMvcRequestBuilders.delete("/products/{id}",1))
                // validate 404 NOT_FOUND received
                .andExpect(status().isNotFound());
    }

}
