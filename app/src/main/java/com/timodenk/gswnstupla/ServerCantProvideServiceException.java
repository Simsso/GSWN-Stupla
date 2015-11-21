package com.timodenk.gswnstupla;

/**
 * Created by Denk on 21/11/15.
 */
public class ServerCantProvideServiceException extends RuntimeException {
    private String serverMessage = null;

    public ServerCantProvideServiceException() {
        super();
    }

    public ServerCantProvideServiceException(String detailMessage) {
        super(detailMessage);
    }

    public ServerCantProvideServiceException(String detailMessage, String serverMessage) {
        super(detailMessage);
        this.serverMessage = serverMessage;
    }

    public ServerCantProvideServiceException(String detailMessage, Throwable throwable) {
        super(detailMessage, throwable);
    }

    public ServerCantProvideServiceException(Throwable throwable) {
        super(throwable);
    }

    public String getServerMessage() {
        return this.serverMessage;
    }
}
