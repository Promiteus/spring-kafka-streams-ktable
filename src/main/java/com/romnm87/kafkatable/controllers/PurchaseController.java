package com.romnm87.kafkatable.controllers;

import com.romnm87.kafkatable.dtos.Purchase;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RequestMapping(value = "/api")
@RestController
public class PurchaseController {

    @PostMapping(value = "/produce")
    public ResponseEntity<List<Purchase>> post(@RequestBody List<Purchase> purchases) {


        return ResponseEntity.ok().body(purchases);
    }
}
