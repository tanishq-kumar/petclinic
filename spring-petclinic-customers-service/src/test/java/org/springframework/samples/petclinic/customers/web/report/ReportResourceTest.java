package org.springframework.samples.petclinic.customers.web.report;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.samples.petclinic.customers.model.report.ReportDimension;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ReportResource.class)
@ActiveProfiles("test")
class ReportResourceTest {

    @Autowired
    MockMvc mvc;

    @MockitoBean
    ReportService reportService;

    @Test
    void postReturnsReportJson() throws Exception {
        given(reportService.generate(any())).willReturn(new OwnerReport(
            ReportDimension.REGION,
            Instant.parse("2026-07-14T00:00:00Z"),
            List.of(new OwnerReport.Row("UK", 4, 5, 3.5, 12, 2.4, LocalDate.of(2024, 7, 15)))));

        mvc.perform(post("/owners/reports")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"dimension\":\"REGION\"}")
                .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.dimension").value("REGION"))
            .andExpect(jsonPath("$.rows[0].group").value("UK"))
            .andExpect(jsonPath("$.rows[0].owners").value(4))
            .andExpect(jsonPath("$.rows[0].pets").value(5))
            .andExpect(jsonPath("$.rows[0].totalVisits").value(12))
            .andExpect(jsonPath("$.rows[0].lastVisitDate").value("2024-07-15"));
    }

    @Test
    void rejectsMissingDimension() throws Exception {
        mvc.perform(post("/owners/reports")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{}")
                .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isBadRequest());
    }
}
