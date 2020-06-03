package client;

import java.io.Serializable;

public class Message implements Serializable
{

    private static final long serialVersionUID = -1669197692562550039L;

    public static final int ONLINE = 0, MESSAGE = 1, EXIT = 2;
    private int type;
    private String message;

    Message(int type, String message)
    {
        this.type = type;
        this.message = message;
    }

    // getters
    public int getType()
    {
        return type;
    }
    public String getMessage()
    {
        return message;
    }
}
