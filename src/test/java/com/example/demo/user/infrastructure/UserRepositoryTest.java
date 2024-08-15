package com.example.demo.user.infrastructure;

import com.example.demo.user.domain.UserStatus;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.jdbc.Sql;

import java.util.Optional;

@DataJpaTest(showSql = true)
@TestPropertySource("classpath:test-application.properties")
@Sql("/sql/user-repository-test-data.sql")
class UserRepositoryTest {

    @Autowired
    private UserRepository userRepository;
    
    @Test
    void findByIdAndStatus_로_유저_데이터를_찾아올_수_있다() {
        Optional<UserEntity> result = userRepository.findByIdAndStatus(1L, UserStatus.ACTIVE);

        Assertions.assertThat(result.isPresent()).isTrue();
    }

    @Test
    void findByIdAndStatus_는_데이터가_없으면_Optional_empty_를_내려준다() {
        Optional<UserEntity> result = userRepository.findByIdAndStatus(10, UserStatus.PENDING);

        Assertions.assertThat(result.isEmpty()).isTrue();
    }

    @Test
    void findByEmailAndStatus_로_유저_데이터를_찾아올_수_있다() {
        Optional<UserEntity> result = userRepository.findByEmailAndStatus("jeongdalma@gmail.com", UserStatus.ACTIVE);

        Assertions.assertThat(result.isPresent()).isTrue();
    }

    @Test
    void findByEmailAndStatus_는_데이터가_없으면_Optional_empty_를_내려준다() {
        Optional<UserEntity> result = userRepository.findByEmailAndStatus("none@gmail.com", UserStatus.PENDING);

        Assertions.assertThat(result.isEmpty()).isTrue();
    }
}
