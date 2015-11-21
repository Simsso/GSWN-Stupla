package com.timodenk.gswnstupla;


class ServerCantProvideServiceException extends RuntimeException {
    private String serverMessage = null;

    public ServerCantProvideServiceException(String detailMessage, String serverMessage) {
        super(detailMessage);
        this.serverMessage = serverMessage;
    }

    public String getServerMessage() {
        return this.serverMessage;
    }
}
