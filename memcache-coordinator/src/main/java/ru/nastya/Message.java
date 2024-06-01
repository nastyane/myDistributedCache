package ru.nastya;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;
import lombok.extern.jackson.Jacksonized;

@Value
@Builder
@Jacksonized
public class Message {
    @JsonProperty("UUID")
    Integer UUID;
    String address;
    String registerNode;
}

