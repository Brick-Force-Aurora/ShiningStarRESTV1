package de.brickforceaurora.restv1.config.common;

import java.util.UUID;

public record ProductInfo(UUID id, String idLong, String idShort, String name) {

    public ProductInfo(UUID id, String name) {
        this(id, id.toString(), id.toString().replace("-", ""), name);
    }

}