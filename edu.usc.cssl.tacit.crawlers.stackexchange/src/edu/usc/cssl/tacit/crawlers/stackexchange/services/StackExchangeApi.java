package edu.usc.cssl.tacit.crawlers.stackexchange.services;

import java.io.IOException;

import okhttp3.HttpUrl;
import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import retrofit2.GsonConverterFactory;
import retrofit2.Retrofit;


public class StackExchangeApi {

    public static final double VERSION = 2.2;
    public static final String ENDPOINT = "https://api.stackexchange.com/" + VERSION+"/";

    private static OkHttpClient.Builder httpClient = new OkHttpClient.Builder();

    private static Retrofit.Builder builder =
            new Retrofit.Builder()
                    .baseUrl(ENDPOINT)
                    .addConverterFactory(GsonConverterFactory.create());

    /**
     * @param site full domain name (ie. "stackoverflow.com"), or a short form identified by api_site_parameter on the site object
     * @return Stack Exchange Per-Site API service
     */
    public StackExchangeSite getSiteService(final String site) {
    	
    	httpClient.addInterceptor(new Interceptor() {
			
			@Override
			public Response intercept(Chain chain) throws IOException {
				Request request = chain.request();
//		        HttpUrl url = request.url().newBuilder().addQueryParameter("site",site).build();
//		        request = request.newBuilder().url(url).build();
		        return chain.proceed(request);
			}
		});

    	return builder
        .client(httpClient.build())
        .build().create(StackExchangeSite.class);
    }

}
