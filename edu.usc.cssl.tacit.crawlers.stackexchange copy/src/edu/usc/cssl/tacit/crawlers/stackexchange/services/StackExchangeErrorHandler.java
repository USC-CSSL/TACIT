package edu.usc.cssl.tacit.crawlers.stackexchange.services;

import retrofit.ErrorHandler;
import retrofit.RetrofitError;
public class StackExchangeErrorHandler implements ErrorHandler {

    @Override
    public Throwable handleError(RetrofitError error) {
        return error;
    }

}
