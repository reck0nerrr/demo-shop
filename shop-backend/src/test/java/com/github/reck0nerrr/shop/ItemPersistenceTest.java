package com.github.reck0nerrr.shop;

import com.github.reck0nerrr.shop.dtos.ItemDtos.*;
import com.github.reck0nerrr.shop.entity.*;
import com.github.reck0nerrr.shop.repositories.*;
import com.github.reck0nerrr.shop.service.ItemService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.context.annotation.Import;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import(ItemService.class) 
@Testcontainers
class ItemPersistenceTest {

    @Container
    @ServiceConnection
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine");

    @Autowired ItemService itemService;
    @Autowired ItemRepository itemRepository;
    @Autowired CharacteristicTypeRepository typeRepository;
    @Autowired CharacteristicValueRepository valueRepository;
    @Autowired OrderRepository orderRepository;
    @Autowired OrderItemRepository orderItemRepository;
    @Autowired UserRepository userRepository;

    @Test
    void imagesLoadInSortOrder() {
        ItemRequest request = new ItemRequest();
        request.setName("T-Shirt");
        request.setPrice(new BigDecimal("19.99"));
        request.setImageUrls(List.of("https://example.com/c.jpg", "https://example.com/a.jpg", "https://example.com/b.jpg"));

        ItemResponse created = itemService.create(request);

        assertEquals(
                List.of("https://example.com/c.jpg", "https://example.com/a.jpg", "https://example.com/b.jpg"),
                created.getImageUrls(),
                "images should preserve submission order via sortOrder"
        );
    }

    @Test
    void characteristicTypesPersistOnCreateAndUpdate() {
        CharacteristicType size = typeRepository.save(CharacteristicType.builder().name("Size").build());

        ItemRequest createRequest = new ItemRequest();
        createRequest.setName("Hoodie");
        createRequest.setPrice(new BigDecimal("39.99"));
        createRequest.setCharacteristicTypeIds(List.of(size.getId()));

        ItemResponse created = itemService.create(createRequest);
        assertEquals(List.of("Size"), created.getCharacteristicTypes());

        ItemResponse reloaded = itemService.getById(created.getId());
        assertEquals(List.of("Size"), reloaded.getCharacteristicTypes(),
                "characteristic types must survive a fresh read, not just appear in the immediate response");

        CharacteristicType color = typeRepository.save(CharacteristicType.builder().name("Color").build());

        ItemRequest updateRequest = new ItemRequest();
        updateRequest.setName("Hoodie");
        updateRequest.setPrice(new BigDecimal("39.99"));
        updateRequest.setCharacteristicTypeIds(List.of(size.getId(), color.getId()));

        itemService.update(created.getId(), updateRequest);

        ItemResponse afterUpdate = itemService.getById(created.getId());
        assertEquals(2, afterUpdate.getCharacteristicTypes().size());
        assertTrue(afterUpdate.getCharacteristicTypes().containsAll(List.of("Size", "Color")));
    }

    @Test
    void replaceVariantsPreservesOrderReferencedVariant() {

        ItemRequest itemRequest = new ItemRequest();
        itemRequest.setName("Mug");
        itemRequest.setPrice(new BigDecimal("12.00"));
        ItemResponse item = itemService.create(itemRequest);

        Long defaultVariantId = item.getVariants().get(0).getId();

        User user = userRepository.save(User.builder()
                .username("buyer").email("buyer@test.com").passwordHash("irrelevant").build());

        Item itemEntity = itemRepository.findById(item.getId()).orElseThrow();
        ItemVariant variantEntity = itemEntity.getVariants().get(0);

        Order order = orderRepository.save(Order.builder()
                .user(user).status(OrderStatus.SHIPPED).total(new BigDecimal("12.00")).build());

        orderItemRepository.save(OrderItem.builder()
                .id(new OrderItemId(order.getId(), defaultVariantId))
                .order(order).variant(variantEntity).quantity(1).price(new BigDecimal("12.00")).build());

        UpdateVariantsRequest resave = new UpdateVariantsRequest();
        VariantRequest row = new VariantRequest();
        row.setStockQuantity(10);
        row.setCharacteristicValueIds(List.of());
        resave.setVariants(List.of(row));

        assertDoesNotThrow(() -> itemService.replaceVariants(item.getId(), resave));

        Item afterReplace = itemRepository.findById(item.getId()).orElseThrow();
        assertTrue(
                afterReplace.getVariants().stream().anyMatch(v -> v.getId().equals(defaultVariantId)),
                "the order-referenced variant must still exist after a replaceVariants call"
        );
        assertTrue(orderItemRepository.findById(new OrderItemId(order.getId(), defaultVariantId)).isPresent(),
                "order history referencing this variant must be untouched");
    }
}
