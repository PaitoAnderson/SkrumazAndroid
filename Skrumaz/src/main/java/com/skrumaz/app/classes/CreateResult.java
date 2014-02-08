package com.skrumaz.app.classes;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Paito Anderson on 2/7/2014.
 */
public class CreateResult {
    private Boolean Success;
    private List<String> Messages;

    public CreateResult() {
        this.Success = false;
        this.Messages = new ArrayList<String>();
    }

    public Boolean getSuccess() {
        return Success;
    }

    public void setSuccess(Boolean success) {
        Success = success;
    }

    public List<String> getAllMessages() {
        return Messages;
    }

    public void addMessage(String message) {
        Messages.add(message);
    }

}
