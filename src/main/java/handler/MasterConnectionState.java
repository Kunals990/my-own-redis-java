package handler;


public class MasterConnectionState {
    public StringBuilder replicationBuffer = new StringBuilder();
    public boolean initialSyncCompleted = false;
    public SyncStage stage = SyncStage.WAITING_FOR_FULLRESYNC;
}
