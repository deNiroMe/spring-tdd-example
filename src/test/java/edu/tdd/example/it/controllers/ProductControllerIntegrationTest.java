package edu.tdd.example.it.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import edu.tdd.example.domain.Product;
import edu.tdd.example.ut.repositories.ProductRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.Arrays;

import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ExtendWith({SpringExtension.class})
public class ProductControllerIntegrationTest {

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private MockMvc mockMvc;

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
    @DisplayName("Test product found - GET /products/1")
    public void testGetProductByIdFindsProduct() throws Exception {

        // perform GET Request
        mockMvc.perform(MockMvcRequestBuilders.get("/products/{id}",1))
                // validate 200 OK and JSON response type is received
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))

                //validate response headers
                .andExpect(header().string(HttpHeaders.ETAG,"\"1\""))
                .andExpect(header().string(HttpHeaders.LOCATION,"/products/1"))

                // validate response body
                .andExpect(jsonPath("$.id",is(1)))
                .andExpect(jsonPath("$.name",is("First Product")))
                .andExpect(jsonPath("$.description",is("First Product Description")))
                .andExpect(jsonPath("$.quantity",is(8)))
                .andExpect(jsonPath("$.version",is(1)));
    }

    @Test
    @DisplayName("Test all products found - GET /products")
    public void testAllProductsFound() throws Exception {

        // perform GET Request
        mockMvc.perform(MockMvcRequestBuilders.get("/products"))
                // validate 200 OK and JSON response type is received
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))

                // validate response body
                .andExpect(jsonPath("$[0].name",is("First Product")))
                .andExpect(jsonPath("$[1].name",is("Second Product")));
    }

    @Test
    @DisplayName("Test add new product - POST /products")
    public void testAddNewProduct() throws Exception {

        // Prepare mock product
        Product newProduct = new Product(10,"New product","New product description",1,1);

        // perform POST Request
        mockMvc.perform(MockMvcRequestBuilders.post("/products")
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .content(new ObjectMapper().writeValueAsString(newProduct))
        )
                // validate 201 CREATED and JSON response type is received
                .andExpect(status().isCreated())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))

                //validate response headers
                .andExpect(header().string(HttpHeaders.ETAG,"\"10\""))
                .andExpect(header().string(HttpHeaders.LOCATION,"/products/10"))

                // validate response body
                .andExpect(jsonPath("$.id",is(10)))
                .andExpect(jsonPath("$.name",is("New product")))
                .andExpect(jsonPath("$.description",is("New product description")))
                .andExpect(jsonPath("$.quantity",is(1)))
                .andExpect(jsonPath("$.version",is(1)));
    }

    @Test
    @DisplayName("Test update existing - PUT /products/1")
    public void testUpdateExistingProduct() throws Exception {

        // Prepare mock product
        Product productToUpdate = new Product("Updated product","Updated product description",10);

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

        // perform PUT Request
        mockMvc.perform(MockMvcRequestBuilders.put("/products/{id}",1)
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .header(HttpHeaders.IF_MATCH,2)
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

        // perform PUT Request
        mockMvc.perform(MockMvcRequestBuilders.put("/products/{id}",100)
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

        // perform DELETE Request
        mockMvc.perform(MockMvcRequestBuilders.delete("/products/{id}",1))
                // validate 200 OK received
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("Test fail to delete a non existing product - DELETE /products/1")
    public void testFailureToDeleteANonExistingProduct() throws Exception {

        // perform DELETE Request
        mockMvc.perform(MockMvcRequestBuilders.delete("/products/{id}",100))
                // validate 404 NOT_FOUND received
                .andExpect(status().isNotFound());
    }

}
