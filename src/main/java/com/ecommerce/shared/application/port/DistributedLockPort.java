package com.ecommerce.shared.application.port;

import java.time.Duration;
import java.util.function.Supplier;

/**
 * Port for distributed locking to ensure consistency across multiple service instances.
 */
public interface DistributedLockPort {

    /**
     * Executes the supplier within a distributed lock.
     *
     * @param lockKey  The unique key for the lock
     * @param waitTime Time to wait for the lock to become available
     * @param leaseTime Time to hold the lock after acquisition
     * @param action   The logic to execute
     * @return The result of the supplier
     * @param <T>      Return type
     * @throws RuntimeException if lock acquisition fails or action throws
     */
    <T> T executeWithLock(String lockKey, Duration waitTime, Duration leaseTime, Supplier<T> action);

    /**
     * Executes the runnable within a distributed lock.
     */
    default void executeWithLock(String lockKey, Duration waitTime, Duration leaseTime, Runnable action) {
        executeWithLock(lockKey, waitTime, leaseTime, () -> {
            action.run();
            return null;
        });
    }
}
