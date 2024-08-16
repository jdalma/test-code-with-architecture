package com.example.demo.user.service;


import com.example.demo.common.domain.exception.CertificationCodeNotMatchedException;
import com.example.demo.common.domain.exception.ResourceNotFoundException;
import com.example.demo.mock.FakeMailSender;
import com.example.demo.mock.FakeUserRepository;
import com.example.demo.mock.TestClockHolder;
import com.example.demo.mock.TestUuidHolder;
import com.example.demo.user.domain.User;
import com.example.demo.user.domain.UserCreate;
import com.example.demo.user.domain.UserStatus;
import com.example.demo.user.domain.UserUpdate;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class UserServiceImplTest {

    private UserServiceImpl userService;

    @BeforeEach
    void setUp() {
        FakeUserRepository repository = new FakeUserRepository();

        dataInit(repository);

        this.userService = new UserServiceImpl(
                repository,
                new CertificationService(new FakeMailSender()),
                new TestUuidHolder("test-code"),
                new TestClockHolder(123456L)
        );
    }

    private void dataInit(FakeUserRepository repository) {
        repository.save(User.builder()
                .id(1L)
                .email("jeongdalma@gmail.com")
                .nickname("jeongdalma")
                .address("Seoul")
                .certificationCode("test-code")
                .status(UserStatus.ACTIVE)
                .lastLoginAt(0L)
                .build());

        repository.save(User.builder()
                .id(2L)
                .email("jeongdalma2@gmail.com")
                .nickname("jeongdalma2")
                .address("Seoul")
                .certificationCode("unknown-code")
                .status(UserStatus.PENDING)
                .lastLoginAt(0L)
                .build());
    }

    @Test
    void getByEmail은_ACTIVE_상태인_유저를_찾아올_수_있다() {
        User result = userService.getByEmail("jeongdalma@gmail.com");
        Assertions.assertThat(result.getNickname()).isEqualTo("jeongdalma");
    }

    @Test
    void getByEmail은_PENDING_상태인_유저를_찾아올_수_없다() {
        Assertions.assertThatThrownBy(() -> {
            userService.getByEmail("jeongdalma2@gmail.com");
        }).isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void getById는_ACTIVE_상태인_유저를_찾아올_수_있다() {
        User result = userService.getById(1L);

        Assertions.assertThat(result.getNickname()).isEqualTo("jeongdalma");
    }

    @Test
    void getById는_PENDING_상태인_유저를_찾아올_수_없다() {

        Assertions.assertThatThrownBy(() -> {
            userService.getById(2L);
        }).isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void 유저를_생성할_수_있다() {
        UserCreate user = UserCreate.builder()
                .email("test@gmail.com")
                .address("Seoul")
                .nickname("test")
                .build();

        User createdUser = userService.create(user);

        Assertions.assertThat(createdUser.getId()).isNotNull();
        Assertions.assertThat(createdUser.getStatus()).isEqualTo(UserStatus.PENDING);
        Assertions.assertThat(createdUser.getCertificationCode()).isEqualTo("test-code");
    }

    @Test
    void 유저를_수정할_수_있다() {
        UserUpdate update = UserUpdate.builder()
                .address("Busan")
                .nickname("test2")
                .build();

        User user = userService.update(1L, update);

        Assertions.assertThat(user.getId()).isNotNull();
        Assertions.assertThat(user.getAddress()).isEqualTo("Busan");
        Assertions.assertThat(user.getNickname()).isEqualTo("test2");
    }

    @Test
    void user를_로그인_시키면_마지막_로그인_시간이_변경된다() {
        userService.login(1L);

        User user = userService.getById(1L);
        Assertions.assertThat(user.getLastLoginAt()).isEqualTo(123456L);
    }

    @Test
    void PENDING_상태의_사용자는_인증_코드로_ACTIVE_시킬_수_있다() {
        userService.verifyEmail(2, "unknown-code");

        User user = userService.getById(2L);
        Assertions.assertThat(user.getStatus()).isEqualTo(UserStatus.ACTIVE);
    }

    @Test
    void PENDING_상태의_사용자는_잘못된_인증_코드를_받으면_예외를_던진다() {
        Assertions.assertThatThrownBy(() -> {
            userService.verifyEmail(2, "aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaab22");
        }).isInstanceOf(CertificationCodeNotMatchedException.class);
    }
}
