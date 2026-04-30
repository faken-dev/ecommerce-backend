package com.ecommerce.shared.infrastructure.lock;

import com.ecommerce.shared.application.port.DistributedLockPort;
import com.ecommerce.shared.exception.BusinessException;
import com.ecommerce.shared.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;


@Slf4j
@Component
@RequiredArgsConstructor
public class RedissonDistributedLock implements DistributedLockPort {

    private final RedissonClient redissonClient;

    @Override
    public <T> T executeWithLock(String lockKey, Duration waitTime, Duration leaseTime, Supplier<T> action) {
        RLock lock = redissonClient.getLock("lock:" + lockKey);
        boolean acquired = false;

        try {
            log.debug("Attempting to acquire lock: {}", lockKey);
            acquired = lock.tryLock(waitTime.toMillis(), leaseTime.toMillis(), TimeUnit.MILLISECONDS);

            if (!acquired) {
                log.warn("Could not acquire lock: {} within {}ms", lockKey, waitTime.toMillis());
                throw new BusinessException(ErrorCode.INTERNAL_SERVER_ERROR, "Operation in progress, please try again later");
            }

            log.debug("Lock acquired: {}", lockKey);
            return action.get();

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new BusinessException(ErrorCode.INTERNAL_SERVER_ERROR, "Lock acquisition interrupted");
        } finally {
            if (acquired && lock.isHeldByCurrentThread()) {
                lock.unlock();
                log.debug("Lock released: {}", lockKey);
            }
        }
    }
}
