package edu.usc.cssl.tacit.crawlers.stackexchange.services;

import edu.usc.cssl.tacit.crawlers.stackexchange.services.types.Error;
import edu.usc.cssl.tacit.crawlers.stackexchange.services.types.Filter;
import edu.usc.cssl.tacit.crawlers.stackexchange.services.types.Site;
import retrofit.http.GET;
import retrofit.http.POST;
import retrofit.http.Path;
import retrofit.http.Query;

public interface StackExchange {

    /**
     * NETWORK METHODS
     *
     * These methods return data across the entire Stack Exchange network of sites. Accordingly, you do not pass a site
     * parameter to them.
     */


    /**
     * Filters
     */

    /**
     * Create a new filter.
     *
     * @see             <a href="https://api.stackexchange.com/docs/create-filter">https://api.stackexchange.com/docs/create-filter</a>
     * @param unsafe
     * @return          response with single filter
     */
    @POST("/filters/create")
    Response<Filter> createFilter(@Query("unsafe") boolean unsafe);

    /**
     * Decode a set of filters, useful for debugging purposes.
     *
     * @see             <a href="https://api.stackexchange.com/docs/read-filter">https://api.stackexchange.com/docs/read-filter</a>
     * @param filters   can contain up to 20 semicolon delimited filters
     * @return          response with filters list
     */
    @GET("/filters/{filters}")
    Response<Filter> getFilters(@Path("filters") String filters);


    /**
     * Errors
     */

    /**
     * Get descriptions of all the errors that the API could return.
     *
     * @see             <a href="https://api.stackexchange.com/docs/errors">https://api.stackexchange.com/docs/errors</a>
     * @return          response with errors list
     */
    @GET("/errors")
    Response<Error> getErrors();

    /**
     * Get descriptions of all the errors that the API could return.
     *
     * @see             <a href="https://api.stackexchange.com/docs/errors">https://api.stackexchange.com/docs/errors</a>
     * @param page      starts at and defaults to 1
     * @param pagesize  can be any value between 0 and 100 and defaults to 30
     * @return          response with errors list
     */
    @GET("/errors")
    Response<Error> getErrors(@Query("page") int page, @Query("pagesize") int pagesize);

    /**
     * Simulate an API error for testing purposes.
     * @TODO handle return result
     *
     * @param id        error id
     */
    @GET("/errors/{id}")
    void getError(@Path("id") int id);


    // Sites

    /**
     * Get all the sites in the Stack Exchange network.
     *
     * @return          response with sites list
     */
    @GET("/sites")
    Response<Site> getSites();

    @GET("/sites")
    Response<Site> getSites(@Query("page") Integer page, @Query("pagesize") Integer pagesize);

    // }}

}
