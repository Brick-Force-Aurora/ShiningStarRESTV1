package de.brickforceaurora.restv1.integration.api.webv2;

import de.brickforceaurora.restv1.config.common.Server;

public interface IApiKeyStorage {

    Server serverByApiKey(String apiKey);

}
