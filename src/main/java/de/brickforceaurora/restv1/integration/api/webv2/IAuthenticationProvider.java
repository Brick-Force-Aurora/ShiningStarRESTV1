package de.brickforceaurora.restv1.integration.api.webv2;

import it.unimi.dsi.fastutil.objects.ObjectList;
import me.lauriichan.snowframe.util.http.HttpMethod;
import me.lauriichan.snowframe.util.http.netty.IAuthenticator;

public interface IAuthenticationProvider {

    IAuthenticator create(IAuthenticator delegate, ObjectList<HttpMethod> methods);

}
