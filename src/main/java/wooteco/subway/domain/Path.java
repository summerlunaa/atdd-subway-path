package wooteco.subway.domain;

import java.util.List;
import java.util.Optional;
import org.jgrapht.GraphPath;
import org.jgrapht.alg.shortestpath.DijkstraShortestPath;
import org.jgrapht.graph.DefaultWeightedEdge;
import org.jgrapht.graph.WeightedMultigraph;
import wooteco.subway.exception.SubwayException;

public class Path {

    private final DijkstraShortestPath<Long, DefaultWeightedEdge> dijkstraShortestPath;

    public Path(DijkstraShortestPath<Long, DefaultWeightedEdge> dijkstraShortestPath) {
        this.dijkstraShortestPath = dijkstraShortestPath;
    }

    public static Path of(List<Section> sections, List<Long> stationIds) {
        WeightedMultigraph<Long, DefaultWeightedEdge> graph = new WeightedMultigraph<>(DefaultWeightedEdge.class);
        addVertexes(stationIds, graph);
        setEdgeWeights(sections, graph);

        return new Path(new DijkstraShortestPath<>(graph));
    }

    private static void addVertexes(List<Long> stationIds, WeightedMultigraph<Long, DefaultWeightedEdge> graph) {
        for (Long stationId : stationIds) {
            graph.addVertex(stationId);
        }
    }

    private static void setEdgeWeights(List<Section> sections, WeightedMultigraph<Long, DefaultWeightedEdge> graph) {
        for (Section section : sections) {
            graph.setEdgeWeight(
                    graph.addEdge(section.getUpStationId(), section.getDownStationId()),
                    section.getDistance());
        }
    }

    public List<Long> findShortestPath(Long source, Long target) {
        return getPath(source, target).getVertexList();
    }

    public int findShortestDistance(Long source, Long target) {
        return (int) getPath(source, target).getWeight();
    }

    private GraphPath<Long, DefaultWeightedEdge> getPath(Long source, Long target) {
        return Optional.ofNullable(dijkstraShortestPath.getPath(source, target))
                .orElseThrow(() -> new SubwayException("경로가 존재하지 않습니다."));
    }
}
