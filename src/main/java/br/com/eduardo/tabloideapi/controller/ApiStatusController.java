package br.com.eduardo.tabloideapi.controller;

import br.com.eduardo.tabloideapi.dto.ApiStatusResponse;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class ApiStatusController {

    @GetMapping(value = "/", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ApiStatusResponse> status() {
        return ResponseEntity.ok(new ApiStatusResponse(
                "UP",
                "Tabloide Extractor API está em execução.",
                "POST /api/v1/tabloides/processar"
        ));
    }
}
