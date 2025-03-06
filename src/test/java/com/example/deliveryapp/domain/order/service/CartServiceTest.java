package com.example.deliveryapp.domain.order.service;

import com.example.deliveryapp.domain.menu.entity.Menu;
import com.example.deliveryapp.domain.menu.entity.OptionCategory;
import com.example.deliveryapp.domain.menu.entity.OptionItem;
import com.example.deliveryapp.domain.menu.repository.MenuRepository;
import com.example.deliveryapp.domain.menu.repository.OptionCategoryRepository;
import com.example.deliveryapp.domain.menu.repository.OptionItemRepository;
import com.example.deliveryapp.domain.order.dto.request.CartAddRequest;
import com.example.deliveryapp.domain.order.dto.request.CartAddRequest.OptionRequest;
import com.example.deliveryapp.domain.order.entity.Order;
import com.example.deliveryapp.domain.order.entity.OrderMenu;
import com.example.deliveryapp.domain.order.enums.OrderState;
import com.example.deliveryapp.domain.order.repository.OrderRepository;
import com.example.deliveryapp.domain.store.entity.Store;
import com.example.deliveryapp.domain.store.enums.StoreStatus;
import com.example.deliveryapp.domain.user.entity.User;
import com.example.deliveryapp.domain.user.enums.UserRole;
import com.example.deliveryapp.domain.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class CartServiceTest {

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private MenuRepository menuRepository;

    @Mock
    private OptionCategoryRepository optionCategoryRepository;

    @Mock
    private OptionItemRepository optionItemRepository;

    @InjectMocks
    private CartService cartService;

    @Nested
    class 장바구니_추가 {

        private CartAddRequest request;

        @BeforeEach
        void setUp() {
            request = new CartAddRequest(
                    1L,
                    List.of(
                            new OptionRequest(1L, List.of(1L, 2L))
                    )
            );
        }

        @Test
        void 장바구니_가게가_다른_경우_초기화_후_추가_성공() {
            User user = mock(User.class);
            given(userRepository.findByIdAndUserRoleOrThrow(anyLong())).willReturn(user);

            Store store = mock(Store.class);
            given(store.getId()).willReturn(1L);

            Menu menu = spy(new Menu("menu1", 10000L, "description", store));
            given(menu.getId()).willReturn(request.getMenuId());
            given(menuRepository.findActiveMenuByIdOrThrow(anyLong())).willReturn(menu);

            OptionItem item1 = spy(new OptionItem("item1", 1000L));
            given(item1.getId()).willReturn(request.getOptions().get(0).getOptionItemIds().get(0));
            OptionItem item2 = spy(new OptionItem("item2", 2000L));
            given(item2.getId()).willReturn(request.getOptions().get(0).getOptionItemIds().get(1));

            OptionCategory optionCategory = spy(new OptionCategory("category1", false, true, null, menu));
            given(optionCategory.getId()).willReturn(request.getOptions().get(0).getOptionCategoryId());
            given(optionCategory.getOptionItems()).willReturn(Arrays.asList(item1, item2));
            given(optionCategoryRepository.findAllByMenuId(anyLong())).willReturn(List.of(optionCategory));

            Store anotherStore = mock(Store.class);
            given(store.getId()).willReturn(2L);
            Order order = spy(new Order(user, anotherStore, OrderState.CART));
            given(order.getId()).willReturn(1L);
            given(orderRepository.findByUserIdAndOrderState(anyLong(),any(OrderState.class)))
                    .willReturn(Optional.of(order));

            given(optionItemRepository.findByIdOrThrow(item1.getId())).willReturn(item1);
            given(optionItemRepository.findByIdOrThrow(item2.getId())).willReturn(item2);

            cartService.addCart(1L, request);

            verify(order, times(1)).clearOrderMenus();
            verify(order, times(1)).setStore(any(Store.class));
        }

        @Test
        void 장바구니_없는_상태_장바구니_추가_성공() {
            User user = mock(User.class);
            given(userRepository.findByIdAndUserRoleOrThrow(anyLong())).willReturn(user);

            Store store = mock(Store.class);
            given(store.getId()).willReturn(1L);

            Menu menu = spy(new Menu("menu1", 10000L, "description", store));
            given(menu.getId()).willReturn(request.getMenuId());
            given(menuRepository.findActiveMenuByIdOrThrow(anyLong())).willReturn(menu);

            OptionItem item1 = spy(new OptionItem("item1", 1000L));
            given(item1.getId()).willReturn(request.getOptions().get(0).getOptionItemIds().get(0));
            OptionItem item2 = spy(new OptionItem("item2", 2000L));
            given(item2.getId()).willReturn(request.getOptions().get(0).getOptionItemIds().get(1));

            OptionCategory optionCategory = spy(new OptionCategory("category1", false, true, null, menu));
            given(optionCategory.getId()).willReturn(request.getOptions().get(0).getOptionCategoryId());
            given(optionCategory.getOptionItems()).willReturn(Arrays.asList(item1, item2));
            given(optionCategoryRepository.findAllByMenuId(anyLong())).willReturn(List.of(optionCategory));

            given(orderRepository.findByUserIdAndOrderState(anyLong(),any(OrderState.class)))
                    .willReturn(Optional.empty());
            given(optionItemRepository.findByIdOrThrow(item1.getId())).willReturn(item1);
            given(optionItemRepository.findByIdOrThrow(item2.getId())).willReturn(item2);

            cartService.addCart(1L, request);

            verify(orderRepository, times(1)).save(any(Order.class));
        }
    }

    @Test
    void testCleanupCartsWhenOrdersExist() {
        // Given
        Long userId1 = 1L;
        User user1 = new User("em@em.com", "pw", "name", UserRole.USER);
        Store store1 = new Store("name", LocalTime.of(9, 0), LocalTime.of(22, 0),
                1000L, StoreStatus.OPEN, user1);
        Order order1 = new Order(user1, store1, OrderState.CART);
        ReflectionTestUtils.setField(order1, "id", 1L);
        order1.setUpdatedAt(LocalDateTime.now().minusHours(25));

        Long userId2 = 2L;
        User user2 = new User("em2@em.com", "pw", "name", UserRole.USER);
        Store store2 = new Store("name2", LocalTime.of(9, 0), LocalTime.of(22, 0),
                1000L, StoreStatus.OPEN, user2);
        Order order2 = new Order(user2, store2, OrderState.CART);
        ReflectionTestUtils.setField(order2, "id", 2L);
        order2.setUpdatedAt(LocalDateTime.now().minusHours(5));
        OrderMenu orderMenu = new OrderMenu(new Menu("menu1", 10000L, "description", store1));
        order1.addOrderMenu(orderMenu);
        order2.addOrderMenu(orderMenu);
        List<Order> orders = Arrays.asList(order1, order2);

        when(orderRepository.findAll()).thenReturn(orders);

        given(orderRepository.findByUserIdAndOrderState(userId1, OrderState.CART)).willReturn(Optional.of(order1));
        given(orderRepository.findByUserIdAndOrderState(userId2, OrderState.CART)).willReturn(Optional.of(order2));

        // When
        cartService.cleanupCarts();

        // Then
        verify(orderRepository, times(1)).findAll();
        verify(orderRepository, times(1)).save(order1);
        verify(orderRepository, times(0)).save(order2);
        assertTrue(order1.getOrderMenus().isEmpty());
        assertFalse(order2.getOrderMenus().isEmpty());
    }

}
