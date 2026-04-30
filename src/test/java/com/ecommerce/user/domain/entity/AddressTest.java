package com.ecommerce.user.domain.entity;

import com.ecommerce.testutil.fixture.Fixtures;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("Address Entity — Domain Tests")
class AddressTest {

    // ── Factory ───────────────────────────────────────────────────────────────

    @Nested
    @DisplayName("create()")
    class Create {

        @Test
        @DisplayName("should create address with correct fields")
        void createsCorrectly() {
            Address address = Fixtures.anAddress();

            assertThat(address.getUserId()).isEqualTo(Fixtures.USER_ID);
            assertThat(address.getRecipientName()).isEqualTo("Test Recipient");
            assertThat(address.getRecipientPhone()).isEqualTo("+84909123456");
            assertThat(address.getAddressLine()).isEqualTo("123 Main Street");
            assertThat(address.getWard()).isEqualTo("Ward 1");
            assertThat(address.getDistrict()).isEqualTo("District 1");
            assertThat(address.getProvince()).isEqualTo("Ho Chi Minh City");
            assertThat(address.isDefaultAddress()).isFalse();
        }

        @Test
        @DisplayName("should create default address when defaultAddress=true")
        void createsAsDefault() {
            Address address = Fixtures.aDefaultAddress();
            assertThat(address.isDefaultAddress()).isTrue();
        }
    }

    // ── Domain Methods ───────────────────────────────────────────────────────

    @Nested
    @DisplayName("update()")
    class Update {

        @Test
        @DisplayName("should update all address fields")
        void updates() {
            Address address = Fixtures.anAddress();

            address.update("New Name", "+84998887766",
                    "789 New Street", "Ward 9", "District 9", "Hanoi");

            assertThat(address.getRecipientName()).isEqualTo("New Name");
            assertThat(address.getRecipientPhone()).isEqualTo("+84998887766");
            assertThat(address.getAddressLine()).isEqualTo("789 New Street");
            assertThat(address.getWard()).isEqualTo("Ward 9");
            assertThat(address.getDistrict()).isEqualTo("District 9");
            assertThat(address.getProvince()).isEqualTo("Hanoi");
            assertThat(address.getUpdatedAt()).isNotNull();
        }
    }

    @Nested
    @DisplayName("markAsDefault() / unmarkAsDefault()")
    class MarkDefault {

        @Test
        @DisplayName("markAsDefault should set defaultAddress=true")
        void marksDefault() {
            Address address = Fixtures.anAddress();
            address.markAsDefault();
            assertThat(address.isDefaultAddress()).isTrue();
        }

        @Test
        @DisplayName("markAsDefault should be idempotent")
        void idempotent() {
            Address address = Fixtures.anAddress();
            address.markAsDefault();
            var first = address.getUpdatedAt();
            address.markAsDefault();
            assertThat(address.getUpdatedAt()).isEqualTo(first);
        }

        @Test
        @DisplayName("unmarkAsDefault should clear default flag")
        void unmarksDefault() {
            Address address = Fixtures.aDefaultAddress();
            address.unmarkAsDefault();
            assertThat(address.isDefaultAddress()).isFalse();
        }
    }

    // ── getFullAddress ───────────────────────────────────────────────────────

    @Nested
    @DisplayName("getFullAddress()")
    class FullAddress {

        @Test
        @DisplayName("should return formatted address string")
        void formats() {
            Address address = Fixtures.anAddress();
            String full = address.getFullAddress();
            assertThat(full).contains("123 Main Street");
            assertThat(full).contains("Ward 1");
            assertThat(full).contains("District 1");
            assertThat(full).contains("Ho Chi Minh City");
        }
    }

    // ── Domain Events ────────────────────────────────────────────────────────

    @Nested
    @DisplayName("Domain Event Factories")
    class DomainEvents {

        @Test
        @DisplayName("toCreatedEvent contains address data")
        void createdEvent() {
            Address address = Fixtures.anAddress();
            var event = address.toCreatedEvent();
            assertThat(event.addressId()).isEqualTo(address.getId());
            assertThat(event.userId()).isEqualTo(Fixtures.USER_ID);
        }

        @Test
        @DisplayName("toMarkedAsDefaultEvent contains address id and user id")
        void markedAsDefaultEvent() {
            Address address = Fixtures.anAddress();
            var event = address.toMarkedAsDefaultEvent(Fixtures.USER_ID);
            assertThat(event.addressId()).isEqualTo(address.getId());
            assertThat(event.userId()).isEqualTo(Fixtures.USER_ID);
        }
    }
}