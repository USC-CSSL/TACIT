package edu.usc.cssl.tacit.crawlers.stackexchange.services;

import java.lang.reflect.Type;
import java.util.Date;

import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;

import retrofit.RequestInterceptor;
import retrofit.RestAdapter;
import retrofit.converter.GsonConverter;

public class StackExchangeApi {

    public static final double VERSION = 2.2;
    public static final String ENDPOINT = "https://api.stackexchange.com/" + VERSION;

    private final String key;
    private final RestAdapter.Builder builder;
    private String accessToken;

    /**
     * Constructs a new StackExchangeApi instance.
     *
     */
    public StackExchangeApi(String key) {
        this.key = key;

        Gson gson = new GsonBuilder()
            .setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES)
            .registerTypeAdapter(Date.class, new JsonDeserializer<Date>() {
                @Override
                public Date deserialize(JsonElement json, Type type, JsonDeserializationContext context) throws JsonParseException {
                    return new Date(json.getAsLong() * 1000);
                }
            })
            .create();
        this.builder = new RestAdapter.Builder()
            .setEndpoint(ENDPOINT)
            .setErrorHandler(new StackExchangeErrorHandler())
            .setLogLevel(RestAdapter.LogLevel.FULL)
            .setConverter(new GsonConverter(gson));

    }

    public void authorize(String accessToken) {
        this.accessToken = accessToken;
    }

    /**
     * @return Stack Exchange network API service
     */
    public StackExchange getService() {
        return builder.build().create(StackExchange.class);
    }

    /**
     * @param site full domain name (ie. "stackoverflow.com"), or a short form identified by api_site_parameter on the site object
     * @return Stack Exchange Per-Site API service
     */
    public StackExchangeSite getSiteService(final String site) {
        return builder
            .setRequestInterceptor(new RequestInterceptor() {
                @Override
                public void intercept(RequestFacade request) {
                    request.addQueryParam("site", site);
//                    request.addQueryParam("key", key);
//                    request.addQueryParam("access_token", accessToken);
                }
            })
            .build()
            .create(StackExchangeSite.class);
    }

}
