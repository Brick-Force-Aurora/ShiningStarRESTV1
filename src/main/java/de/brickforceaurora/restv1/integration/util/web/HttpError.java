package de.brickforceaurora.restv1.integration.util.web;

import java.io.IOException;

import me.lauriichan.laylib.json.JsonObject;
import me.lauriichan.snowframe.util.http.HttpCode;
import me.lauriichan.snowframe.util.http.HttpData;
import me.lauriichan.snowframe.util.http.server.Response;

public final class HttpError {

    public static final int IO_ERROR = 1;
    public static final String IO_ERROR_NAME = "IO_ERROR";

    public static final int VERSION_MISMATCH = 2;
    public static final String VERSION_MISMATCH_NAME = "VERSION_MISMATCH";

    public static final int MISSING_DATA = 3;
    public static final String MISSING_DATA_NAME = "MISSING_DATA";

    public static final int UNKNOWN = 4;
    public static final String UNKNOWN_NAME = "UNKNOWN";

    private HttpError() {
        throw new UnsupportedOperationException();
    }

    public static Response generic(HttpCode code, int errorCode, String errorName, String message) {
        JsonObject errorObject = new JsonObject();
        errorObject.put("code", errorCode);
        errorObject.put("name", errorName);
        errorObject.put("message", message);
        return HttpResp.generic(code, HttpResp.ERROR, HttpResp.ERROR_NAME, "error", errorObject);
    }

    public static Response serverError(int code, String errorName, String message) {
        return generic(HttpCode.INTERNAL_SERVER_ERROR, code, errorName, message);
    }

    public static Response serverError(String message, IOException exp) {
        return generic(HttpCode.INTERNAL_SERVER_ERROR, IO_ERROR, IO_ERROR_NAME, message + "; " + exp.getMessage());
    }

    public static Response notFound(String message) {
        return generic(HttpCode.NOT_FOUND, UNKNOWN, UNKNOWN_NAME, message);
    }

    public static Response badRequest(int code, String errorName, String message) {
        return generic(HttpCode.BAD_REQUEST, code, errorName, message);
    }

    public static Response badRequest(HttpData<?> body) {
        return badRequest(IO_ERROR, IO_ERROR_NAME, body.error().getMessage());
    }

}
