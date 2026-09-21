package br.com.eduardo.tabloideapi.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ApiStatusController.class)
class ApiStatusControllerTests {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void informaQueApiEstaEmExecucao() throws Exception {
        mockMvc.perform(get("/"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith("application/json"))
                .andExpect(jsonPath("$.status").value("UP"))
                .andExpect(jsonPath("$.mensagem")
                        .value("Tabloide Extractor API está em execução."))
                .andExpect(jsonPath("$.processamento")
                        .value("POST /api/v1/tabloides/processar"));
    }
}
