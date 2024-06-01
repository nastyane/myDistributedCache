package ru.nastya;

import lombok.Builder;
import lombok.Value;
import lombok.extern.jackson.Jacksonized;

@Value
@Builder
@Jacksonized
public class Information {
    Integer UUID;
    String info;
}
