package de.brickforceaurora.restv1.integration.api.web;

import com.sun.net.httpserver.Authenticator;

import it.unimi.dsi.fastutil.objects.ObjectList;

public interface IAuthenticationProvider {
    
    Authenticator create(Authenticator delegate, ObjectList<String> methods);

}
