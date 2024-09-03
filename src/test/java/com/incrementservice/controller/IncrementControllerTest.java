package com.incrementservice.controller;

import com.incrementservice.service.IncrementService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class IncrementControllerTest {

    @Mock
    private IncrementService incrementService;

    @InjectMocks
    private IncrementController incrementController;

    private MockMvc mockMvc;

    /**
     * Sets up the MockMvc instance before each test.
     * <p>
     * This method is executed before each test case is run, ensuring that
     * the {@link MockMvc} instance is properly initialized with the
     * {@link com.incrementservice.controller.IncrementController} controller.
     * This setup allows for standalone testing of the controller
     * without starting the full Spring context.
     * </p>
     */
    @BeforeEach
    public void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(incrementController).build();
    }

    /**
     * Test case to verify the successful behavior of the increment operation.
     * This test expects the API to return a 202 Accepted status
     *
     * @throws Exception if there is an error during the request execution.
     */
    @Test
    public void testIncrementValue_Success() throws Exception {
        mockMvc = MockMvcBuilders.standaloneSetup(incrementController).build();

        // Perform the request with valid key and value and capture the actual response
        mockMvc.perform(post("/api/increment")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"key\":\"transactionA\",\"value\":10}"))
                .andExpect(status().isAccepted())
                .andReturn();
    }

    /**
     * Test case to verify the behavior when the 'key' is missing in the request body.
     * This test expects the API to return a 400 Bad Request status.
     *
     * @throws Exception if there is an error during the request execution.
     */
    @Test
    public void testIncrementValue_MissingKey() throws Exception {

        // Perform the request with a missing key and capture the actual response
        mockMvc.perform(post("/api/increment")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"value\":10}"))
                .andExpect(status().isBadRequest());
    }

    /**
     * Test case to verify the behavior when the 'value' is missing in the request body.
     * This test expects the API to return a 400 Bad Request status.
     *
     * @throws Exception if there is an error during the request execution.
     */
    @Test
    public void testIncrementValue_MissingValue() throws Exception {

        // Perform the request with a missing value and capture the actual response
        mockMvc.perform(post("/api/increment")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"key\":\"transactionB\"}"))
                .andExpect(status().isBadRequest());
    }

    /**
     * Test case to verify the behavior when both 'key' and 'value' are missing in the request body.
     * This test expects the API to return a 400 Bad Request status.
     *
     * @throws Exception if there is an error during the request execution.
     */
    @Test
    public void testIncrementValue_MissingKeyAndValue() throws Exception {

        // Perform the request with both key and value missing and capture the actual response
        mockMvc.perform(post("/api/increment")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest());
    }
}
