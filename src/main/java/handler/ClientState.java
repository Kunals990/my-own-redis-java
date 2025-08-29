package handler;

import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

public class ClientState {
    public StringBuilder readBuffer = new StringBuilder();

    public boolean inTransaction = false;
    public Queue<List<String>> transactionQueue = new LinkedList<>();

    public boolean inSubscribedMode = false;

}