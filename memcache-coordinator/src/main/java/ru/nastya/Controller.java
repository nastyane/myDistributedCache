package ru.nastya;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.web.bind.annotation.*;

import java.beans.JavaBean;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.UUID;

@RestController
@RequestMapping("/api")
public class Controller {
    private final HashMap<String, String> table = new HashMap<>();

    @PostMapping("/approve")
    public ResponseEntity<String> approve(@RequestBody RegisterRequest request) {
        if (table.containsValue(request.getAddress())) {
            return ResponseEntity.ok("the node is registered");
        } else {
            table.put(UUID.randomUUID().toString(), request.getAddress());
            return ResponseEntity.ok("approve");
        }
    }

    @GetMapping("/registrationTable")
    public HashMap<String, String> registrationTable() {
        return table;
    }

}
