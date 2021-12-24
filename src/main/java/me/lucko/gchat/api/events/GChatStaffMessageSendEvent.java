package me.lucko.gchat.api.events;

import com.velocitypowered.api.event.ResultedEvent;

public class GChatStaffMessageSendEvent {

    private final String name;
    private final String message;
    private final String formattedMessage;
    private ResultedEvent.GenericResult result;

    public GChatStaffMessageSendEvent(String name, String message, String formattedMessage) {
        this.name = name;
        this.message = message;
        this.formattedMessage = formattedMessage;
        this.result = ResultedEvent.GenericResult.allowed();
    }

    public String getName() {
        return name;
    }

    public String getMessage() {
        return message;
    }

    public String getFormattedMessage() {
        return formattedMessage;
    }

    public ResultedEvent.GenericResult getResult() {
        return result;
    }

    public void setResult(ResultedEvent.GenericResult result) {
        this.result = result;
    }
}