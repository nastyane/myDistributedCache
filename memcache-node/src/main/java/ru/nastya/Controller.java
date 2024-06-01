package ru.nastya;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestClient;

import java.util.HashMap;

@Slf4j
@RestController
@RequestMapping("/api")
public class Controller {
    HashMap<Integer, String> hashTable = new HashMap<>();

    @Value("${memcache.coordinator.adress}")
    private String coordinatorAdress;
    @Value("${advertisement-address}")
    private String registrarAddress;

    @GetMapping("/information")
    public ResponseEntity<String> approve(@RequestBody Information hashInfo) {
        hashTable.put(hashInfo.getUUID(), hashInfo.getInfo());
        return ResponseEntity.ok("OK");
    }

//        @Scheduled(fixedRate = 1000)



    @Scheduled(fixedRate = 1000)
    public void scheduleFixedRateTask() {
        RestClient defaultClient = RestClient.create();
        String result = defaultClient.post()
                .uri("http://localhost:8080/api/approve")
                .body(new RegisterRequest(registrarAddress))
                .retrieve()
                .body(String.class);
        System.out.println(result);
        //System.out.println("Fixed rate task - " + System.currentTimeMillis() / 1000);
    }
}
