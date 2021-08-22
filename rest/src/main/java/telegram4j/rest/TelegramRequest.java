package telegram4j.rest;

import reactor.util.annotation.Nullable;
import telegram4j.rest.route.Route;

import java.util.*;

public class TelegramRequest {

    private final Route route;

    @Nullable
    private Object body;
    @Nullable
    private Map<String, Set<String>> headers;

    public TelegramRequest(Route route) {
        this.route = Objects.requireNonNull(route, "route");
    }

    public Route getRoute() {
        return route;
    }

    @Nullable
    public Object getBody() {
        return body;
    }

    public TelegramRequest body(@Nullable Object body) {
        this.body = body;
        return this;
    }

    @Nullable
    public Map<String, Set<String>> getHeaders() {
        return headers;
    }

    public TelegramRequest header(String name, String header) {
        if (headers == null) {
            headers = new LinkedHashMap<>();
        }
        headers.computeIfAbsent(name, k -> new LinkedHashSet<>()).add(header);
        return this;
    }

    public TelegramResponse exchange(RestRouter router) {
        return router.exchange(this);
    }
}
