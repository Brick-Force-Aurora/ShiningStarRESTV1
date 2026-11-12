package de.brickforceaurora.restv1.integration.updater;

import java.io.IOException;

public class FailedToBuildUpdateException extends IOException {

    private static final long serialVersionUID = 5083978405419988113L;
    
    public FailedToBuildUpdateException() {
        super();
    }
    
    public FailedToBuildUpdateException(Throwable cause) {
        super(cause);
    }
    
    public FailedToBuildUpdateException(String message) {
        super(message);
    }
    
    public FailedToBuildUpdateException(String message, Throwable cause) {
        super(message, cause);
    }

}
