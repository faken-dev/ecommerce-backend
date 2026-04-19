package com.ecommerce.order.domain.entity;

import java.util.EnumMap;
import java.util.EnumSet;
import java.util.Map;
import java.util.Set;

/**
 * Defines all valid {@link OrderStatus} transitions in one place.
 * Adding a new status or transition requires no changes to this class —
 * only the transition map needs updating.
 *
 * <p>Violations of OCP in the original switch-based {@code canTransitionTo()}:
 * <ul>
 *   <li>Adding a new status: must modify every switch branch (N places)</li>
 *   <li>Adding a new transition: must modify the source status's branch</li>
 * </ul>
 * With this table, both cases require only one entry in the map.
 */
public final class OrderStatusTransitionTable {

    private static final Map<OrderStatus, Set<OrderStatus>> TRANSITIONS = buildTransitions();

    private OrderStatusTransitionTable() {}

    private static Map<OrderStatus, Set<OrderStatus>> buildTransitions() {
        Map<OrderStatus, Set<OrderStatus>> table = new EnumMap<>(OrderStatus.class);

        table.put(OrderStatus.PENDING,     EnumSet.of(OrderStatus.CONFIRMED, OrderStatus.CANCELLED));
        table.put(OrderStatus.CONFIRMED,   EnumSet.of(OrderStatus.PROCESSING, OrderStatus.CANCELLED));
        table.put(OrderStatus.PROCESSING,  EnumSet.of(OrderStatus.SHIPPED));
        table.put(OrderStatus.SHIPPED,     EnumSet.of(OrderStatus.DELIVERED));
        table.put(OrderStatus.DELIVERED,   EnumSet.of(OrderStatus.REFUNDED, OrderStatus.PARTIALLY_REFUNDED));
        // Terminal states: no valid outgoing transitions
        table.put(OrderStatus.CANCELLED,          EnumSet.noneOf(OrderStatus.class));
        table.put(OrderStatus.REFUNDED,           EnumSet.noneOf(OrderStatus.class));
        table.put(OrderStatus.PARTIALLY_REFUNDED, EnumSet.noneOf(OrderStatus.class));

        return Map.copyOf(table);
    }

    /**
     * Returns the set of valid target statuses from {@code from}.
     * Returns an immutable view.
     */
    public static Set<OrderStatus> allowedFrom(OrderStatus from) {
        return TRANSITIONS.getOrDefault(from, EnumSet.noneOf(OrderStatus.class));
    }

    /**
     * Returns {@code true} if transitioning from {@code from} → {@code to} is valid.
     */
    public static boolean canTransition(OrderStatus from, OrderStatus to) {
        Set<OrderStatus> allowed = allowedFrom(from);
        return allowed.contains(to);
    }

    /** Returns {@code true} if {@code status} is a terminal state (no outgoing edges). */
    public static boolean isTerminal(OrderStatus status) {
        return allowedFrom(status).isEmpty();
    }
}
