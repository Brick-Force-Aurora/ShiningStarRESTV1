package de.brickforceaurora.restv1.integration.api.webv2;

public interface IVariableType {
    
    Object resolve(HttpHeaders headers, HttpString segment);

}
