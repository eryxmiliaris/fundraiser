package com.vb.fundraiser.controller;

import com.vb.fundraiser.model.request.AddMoneyRequest;
import com.vb.fundraiser.model.dto.CollectionBoxDTO;
import com.vb.fundraiser.service.CollectionBoxService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/boxes")
@RequiredArgsConstructor
public class CollectionBoxController {
    private final CollectionBoxService boxService;

    @PostMapping
    public ResponseEntity<CollectionBoxDTO> registerNewBox() {
        return ResponseEntity.ok(boxService.registerNewBox());
    }

    @GetMapping
    public ResponseEntity<List<CollectionBoxDTO>> getAllBoxes() {
        return ResponseEntity.ok(boxService.getAllBoxes());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<String> unregisterBox(@PathVariable Long id) {
        boxService.unregisterBox(id);
        return ResponseEntity.ok("Box " + id + " successfully unregistered");
    }

    @PutMapping("/{id_box}/events/{id_event}")
    public ResponseEntity<CollectionBoxDTO> assignBoxToEvent(
            @PathVariable Long id_box,
            @PathVariable Long id_event
    ) {
        return ResponseEntity.ok(boxService.assignBoxToEvent(id_box, id_event));
    }

    @PutMapping("/{id}/add-money")
    public ResponseEntity<String> addMoney(
            @PathVariable Long id,
            @RequestBody @Valid AddMoneyRequest request
    ) {
        boxService.addMoney(id, request.currencyCode(), request.amount());
        return ResponseEntity.ok("Money successfully added to the box " + id);
    }

    @PostMapping("/{id}/empty")
    public ResponseEntity<String> emptyBox(@PathVariable Long id) {
        boxService.emptyBox(id);
        return ResponseEntity.ok("Box " + id + " successfully emptied");
    }
}
