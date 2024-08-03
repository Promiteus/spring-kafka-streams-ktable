package com.romnm87.kafkatable.controllers;

import com.romnm87.kafkatable.dtos.Purchase;
import com.romnm87.kafkatable.services.ProduceService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RequestMapping(value = "/api")
@RestController
public class PurchaseController {
    private final ProduceService produceService;

    public PurchaseController(ProduceService produceService) {
        this.produceService = produceService;
    }

    @PostMapping(value = "/purchases")
    public ResponseEntity<List<Purchase>> post(@RequestBody List<Purchase> purchases) {
        this.produceService.sendMessage(null, purchases);
        return ResponseEntity.ok().body(purchases);
    }

    @GetMapping(value = "/purchases/groups")
    public ResponseEntity<?> get() {
        List<Purchase> purchases = this.produceService.getPurchaseGroups();
        return ResponseEntity.ok().body(purchases);
    }
}
