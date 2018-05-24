package com.statistics.statistics;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.statistics.statistics.controller.WebController;
import com.statistics.statistics.model.StatisticsSnapshot;
import com.statistics.statistics.model.Transaction;
import com.statistics.statistics.service.TransactionService;
import org.eclipse.jetty.webapp.WebAppContext;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.http.MediaType;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringRunner.class)
@ContextConfiguration(classes = {WebAppContext.class})
@WebAppConfiguration
@ComponentScan({
        "com.statistics.statistics.service",
        "com.statistics.statistics.repository",
        "com.statistics.statistics.controller"
})
public class WebControllerTests {

    @Mock
    private TransactionService transactionService;

    private MockMvc mockMvc;

    @InjectMocks
    WebController webController;

    @Before
    public void setup() {

        MockitoAnnotations.initMocks(this);
        this.mockMvc = MockMvcBuilders.standaloneSetup(webController).build();

    }

    @Test
    public void postTransaction_TransactionIsCreated_True() throws Exception {

        long currentTime = System.currentTimeMillis() / 1000L;
        Double amount = 12.0;

        Transaction transaction = new Transaction(currentTime, amount);

        mockMvc.perform(post("/transactions")
                .contentType(MediaType.APPLICATION_JSON)
                .content(asJsonString(transaction)))
            .andExpect(status().isCreated());

    }

    @Test
    public void getStatistics_OkRequest_True() throws Exception {

        mockMvc.perform(get("/statistics"))
                .andExpect(status().isOk());
    }


    public static String asJsonString(final Object obj) {
        try {
            final ObjectMapper mapper = new ObjectMapper();
            final String jsonContent = mapper.writeValueAsString(obj);
            return jsonContent;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

}
