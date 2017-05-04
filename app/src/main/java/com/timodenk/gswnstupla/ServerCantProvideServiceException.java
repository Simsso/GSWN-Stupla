package com.timodenk.gswnstupla;


class ServerCantProvideServiceException extends RuntimeException {
    private String serverMessage = null;

    ServerCantProvideServiceException(String detailMessage, String serverMessage) {
        super(detailMessage);
        this.serverMessage = serverMessage;
    }

    String getServerMessage() {
        return this.serverMessage;
    }
}
