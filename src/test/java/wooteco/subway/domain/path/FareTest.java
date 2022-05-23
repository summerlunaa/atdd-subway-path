package wooteco.subway.domain.path;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class FareTest {

    @DisplayName("요금을 계산한다.")
    @CsvSource(value = {"9:1250", "10:1250", "11:1350", "33:1750", "49:2050", "50:2050", "51:2150", "58:2150"},
            delimiter = ':')
    @ParameterizedTest
    void calculate(int distance, int expected) {
        Fare fare = new Fare(distance);

        assertThat(fare.calculate()).isEqualTo(expected);
    }
}
