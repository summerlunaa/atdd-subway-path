package wooteco.subway.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.tuple;
import static org.junit.jupiter.api.Assertions.assertAll;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.jdbc.core.JdbcTemplate;
import wooteco.subway.dao.line.JdbcLineDao;
import wooteco.subway.dao.line.LineDao;
import wooteco.subway.dao.section.JdbcSectionDao;
import wooteco.subway.dao.section.SectionDao;
import wooteco.subway.dao.station.JdbcStationDao;
import wooteco.subway.dao.station.StationDao;
import wooteco.subway.domain.line.Line;
import wooteco.subway.domain.section.Section;
import wooteco.subway.domain.station.Station;
import wooteco.subway.dto.path.PathRequest;
import wooteco.subway.dto.path.PathResponse;
import wooteco.subway.dto.station.StationResponse;
import wooteco.subway.exception.DataNotExistException;
import wooteco.subway.exception.SubwayException;

@JdbcTest
class PathServiceTest {

    private Long stationId1;
    private Long stationId2;
    private Long stationId3;
    private Long stationId4;
    private Long stationId5;
    private PathRequest pathRequest;

    private PathService pathService;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @BeforeEach
    void setUp() {
        StationDao stationDao = new JdbcStationDao(jdbcTemplate);
        stationId1 = stationDao.save(new Station("선릉역"));
        stationId2 = stationDao.save(new Station("잠실역"));
        stationId3 = stationDao.save(new Station("역삼역"));
        stationId4 = stationDao.save(new Station("강남역"));
        stationId5 = stationDao.save(new Station("교대역"));

        LineDao lineDao = new JdbcLineDao(jdbcTemplate);
        Long lineId1 = lineDao.save(new Line("일호선", "bg-red-600", 200));
        Long lineId2 = lineDao.save(new Line("이호선", "bg-green-600", 300));

        SectionDao sectionDao = new JdbcSectionDao(jdbcTemplate);
        sectionDao.save(new Section(1L, lineId1, stationId1, stationId2, 2));
        sectionDao.save(new Section(2L, lineId1, stationId2, stationId3, 2));
        sectionDao.save(new Section(3L, lineId1, stationId3, stationId4, 7));
        sectionDao.save(new Section(4L, lineId2, stationId2, stationId5, 3));
        sectionDao.save(new Section(5L, lineId2, stationId5, stationId4, 4));


        StationService stationService = new StationService(stationDao);
        SectionService sectionService = new SectionService(sectionDao, stationService);
        LineService lineService = new LineService(lineDao, sectionService);
        pathService = new PathService(stationService, lineService, sectionService);
    }

    @DisplayName("경로를 조회한다.")
    @Test
    void findPath() {
        pathRequest = new PathRequest(stationId1, stationId4, 10);

        PathResponse pathResponse = pathService.findPath(pathRequest);

        assertAll(
                () -> assertThat(pathResponse.getStations())
                        .extracting(StationResponse::getId, StationResponse::getName)
                        .containsExactly(
                                tuple(stationId1, "선릉역"),
                                tuple(stationId2, "잠실역"),
                                tuple(stationId5, "교대역"),
                                tuple(stationId4, "강남역")
                        ),
                () -> assertThat(pathResponse.getDistance())
                        .isEqualTo(9),
                () -> assertThat(pathResponse.getFare())
                        .isEqualTo(600)
        );
    }

    @DisplayName("출발역과 도착역이 같은 경우 예외가 발생한다.")
    @Test
    void findPathSameTargetAndSource() {
        pathRequest = new PathRequest(stationId1, stationId1, 0);

        assertThatThrownBy(() -> pathService.findPath(pathRequest))
                .isInstanceOf(SubwayException.class)
                .hasMessage("출발역과 도착역이 같을 수 없습니다.");
    }

    @DisplayName("역이 존재하지 않는 경우 예외가 발생한다.")
    @Test
    void findPathNotExistStation() {
        pathRequest = new PathRequest(100L, stationId1, 0);

        assertThatThrownBy(() -> pathService.findPath(pathRequest))
                .isInstanceOf(DataNotExistException.class)
                .hasMessage("존재하지 않는 역입니다.");
    }
}
