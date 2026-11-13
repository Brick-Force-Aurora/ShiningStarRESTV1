package de.brickforceaurora.restv1.integration.api.webv2;

import me.lauriichan.snowframe.util.http.type.HttpContentType;

public interface IWebProvider {

    <T> IWebContext<T> createContext(HttpContentType<T> contentType);

    <T> IWebContext<T> createContext(String basePath, HttpContentType<T> contentType);

}
