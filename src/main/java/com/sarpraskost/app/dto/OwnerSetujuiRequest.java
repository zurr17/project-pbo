package com.sarpraskost.app.dto;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonProperty;

public record OwnerSetujuiRequest(
    @JsonProperty("mekanik_id") @JsonAlias("mekanikId") Long mekanikId,
    String prioritas,
    @JsonProperty("catatan_owner") @JsonAlias("catatanOwner") String catatanOwner
) {
}
