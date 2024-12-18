package com.example.foodstore.controlleradmin;

import com.example.foodstore.repository.OrderDetailRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.Arrays;
import java.util.List;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
public class StatisticsControllerTest {

    private MockMvc mockMvc;

    @Mock
    private OrderDetailRepository orderDetailRepository;

    @InjectMocks
    private StatisticsController statisticsController;

    @BeforeEach
    public void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(statisticsController).build();
    }

    @Test
    public void testReportProduct() throws Exception {
        List<Object[]> mockReport = Arrays.asList(
                new Object[]{"Product A", 100, 5000},
                new Object[]{"Product B", 200, 10000}
        );
        when(orderDetailRepository.repo()).thenReturn(mockReport);

        mockMvc.perform(get("/admin/report-product"))
                .andExpect(status().isOk())
                .andExpect(view().name("admin/statistical"))
                .andExpect(model().attribute("reportData", mockReport))
                .andExpect(model().attribute("reportType", "Sản phẩm"));
    }

    @Test
    public void testReportCategory() throws Exception {
        List<Object[]> mockReport = Arrays.asList(
                new Object[]{"Category A", 150, 7000},
                new Object[]{"Category B", 250, 12000}
        );
        when(orderDetailRepository.repoWhereCategory()).thenReturn(mockReport);

        mockMvc.perform(get("/admin/report-category"))
                .andExpect(status().isOk())
                .andExpect(view().name("admin/statistical"))
                .andExpect(model().attribute("reportData", mockReport))
                .andExpect(model().attribute("reportType", "Thể loại"));
    }

    @Test
    public void testShowCharts() throws Exception {
        List<Object[]> mockProductData = Arrays.asList(
                new Object[]{"Product A", 100, 5000},
                new Object[]{"Product B", 200, 10000}
        );

        List<Object[]> mockCategoryData = Arrays.asList(
                new Object[]{"Category A", 150, 7000},
                new Object[]{"Category B", 250, 12000}
        );

        List<Object[]> mockYearData = Arrays.asList(
                new Object[]{"2024", 1000, 50000},
                new Object[]{"2023", 800, 40000}
        );

        when(orderDetailRepository.repo()).thenReturn(mockProductData);
        when(orderDetailRepository.repoWhereCategory()).thenReturn(mockCategoryData);
        when(orderDetailRepository.repoWhereYear()).thenReturn(mockYearData);

        ObjectMapper mapper = new ObjectMapper();

        mockMvc.perform(get("/admin/charts"))
                .andExpect(status().isOk())
                .andExpect(view().name("admin/charts"))
                .andExpect(model().attribute("productDataJson", mapper.writeValueAsString(mockProductData)))
                .andExpect(model().attribute("categoryDataJson", mapper.writeValueAsString(mockCategoryData)))
                .andExpect(model().attribute("yearDataJson", mapper.writeValueAsString(mockYearData)));
    }
}
