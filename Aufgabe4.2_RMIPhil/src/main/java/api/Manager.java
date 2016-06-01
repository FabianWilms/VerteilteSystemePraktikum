package api;

import java.rmi.Remote;
import java.util.List;

/**
 * Centralized Manager to manage the restaurant. Is run together with {@link BindingProxy} and the central RMIRegistry on one Machine.
 */
public interface Manager extends Remote {
    /**
     * Name to be used to register in the RMI
     */
    public static final String NAME = "manager";

    /**
     * Registers a {@link TablePart} in the restaurant
     *
     * @param uid The uid with which the @{@link TablePart} is stored in the RMI
     */
    public void registerTablepart(String uid);

    /**
     * Registers a {@link Philosopher} in the restaurant
     *
     * @param uid The uid with which the {@link Philosopher} is stored in the RMI
     */
    public void registerPhilosopher(String uid);

    /**
     * Get all registered {@link Philosopher}
     *
     * @return List of all registered {@link Philosopher} UIDs
     */
    public List<String> getPhilosophers();

    /**
     * Returns the next tableparts UID. To be used by {@link TablePart} to "close" the table.
     *
     * @param myUid The UID of this table.
     * @return The UID of the next table.
     */
    public String getNextTablePart(String myUid);

    /**
     * Returns a random table parts UID. To be used by {@link Philosopher} to get the first table part to look for a free seat.
     *
     * @return A random table parts UID.
     */
    public String getRandomTablePart();
}
