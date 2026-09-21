package br.com.eduardo.tabloideapi.controller;

import br.com.eduardo.tabloideapi.dto.TabloideResponse;
import br.com.eduardo.tabloideapi.service.TabloideService;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/v1/tabloides")
public class TabloideController {

    private final TabloideService tabloideService;

    public TabloideController(TabloideService tabloideService) {
        this.tabloideService = tabloideService;
    }

    @PostMapping(
            value = "/processar",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    public ResponseEntity<TabloideResponse> processar(
            @RequestParam("file") MultipartFile file) {

        return ResponseEntity.ok(
                tabloideService.processar(file)
        );
    }
}
