package de.brickforceaurora.restv1.integration.api.webv2;

import me.lauriichan.snowframe.extension.ExtensionPoint;
import me.lauriichan.snowframe.extension.IExtension;

@ExtensionPoint
public interface IWebExtension extends IExtension {
    
    default WebRoute[] createRoutes() {
        return null;
    }

}
