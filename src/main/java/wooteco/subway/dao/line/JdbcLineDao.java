package wooteco.subway.dao.line;

import java.sql.PreparedStatement;
import java.util.List;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;
import wooteco.subway.domain.line.Line;

@Repository
public class JdbcLineDao implements LineDao {

    private static final RowMapper<Line> LINE_ROW_MAPPER = (resultSet, rowNum) -> new Line(
            resultSet.getLong("id"),
            resultSet.getString("name"),
            resultSet.getString("color"),
            resultSet.getInt("extra_fare")
    );

    private final JdbcTemplate jdbcTemplate;

    public JdbcLineDao(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public long save(Line line) {
        final String sql = "insert into LINE (name, color, extra_fare) values (?, ?, ?)";

        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(connection -> {
            PreparedStatement preparedStatement = connection.prepareStatement(sql, new String[]{"id"});
            preparedStatement.setString(1, line.getName());
            preparedStatement.setString(2, line.getColor());
            preparedStatement.setInt(3, line.getExtraFare());
            return preparedStatement;
        }, keyHolder);

        return keyHolder.getKey().longValue();
    }

    @Override
    public boolean existLineById(Long id) {
        final String sql = "select exists (select * from LINE where id = ?)";
        return Boolean.TRUE.equals(jdbcTemplate.queryForObject(sql, Boolean.class, id));
    }

    @Override
    public List<Line> findAll() {
        final String sql = "select id, name, color, extra_fare from LINE";
        return jdbcTemplate.query(sql, LINE_ROW_MAPPER);
    }

    @Override
    public Line findById(Long id) {
        final String sql = "select id, name, color, extra_fare from LINE where id = ?";
        return jdbcTemplate.queryForObject(sql, LINE_ROW_MAPPER, id);
    }

    @Override
    public void update(Line line) {
        final String sql = "update LINE set name = ?, color = ?, extra_fare = ? where id = ?";
        jdbcTemplate.update(sql, line.getName(), line.getColor(), line.getExtraFare(), line.getId());
    }

    @Override
    public void delete(Long id) {
        final String sql = "delete from LINE where id = ?";
        jdbcTemplate.update(sql, id);
    }
}
