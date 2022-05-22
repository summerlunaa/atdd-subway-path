package wooteco.subway.domain.section;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import wooteco.subway.exception.DataNotExistException;
import wooteco.subway.exception.SubwayException;

public class Sections {

    private final List<Section> sections;

    public Sections(List<Section> sections) {
        this.sections = sortSections(sections);
    }

    private List<Section> sortSections(List<Section> sections) {
        if (sections.isEmpty()) {
            return sections;
        }

        List<Section> sortedSections = new ArrayList<>();
        Long topStationId = getTopStationId(sections);

        for (int i = 0; i < sections.size(); i++) {
            Section nextSection = getNextSection(sections, topStationId);
            sortedSections.add(nextSection);
            topStationId = nextSection.getDownStationId();
        }

        return sortedSections;
    }

    private Long getTopStationId(List<Section> sections) {
        Set<Long> downStationIds = getDownStationIds(sections);
        return sections.stream()
                .map(Section::getUpStationId)
                .filter(sectionId -> !downStationIds.contains(sectionId))
                .findAny()
                .orElseThrow(() -> new DataNotExistException("상행종점을 찾을 수 없습니다."));
    }

    private Set<Long> getDownStationIds(List<Section> sections) {
        return sections.stream()
                .map(Section::getDownStationId)
                .collect(Collectors.toSet());
    }

    private Section getNextSection(List<Section> sections, Long topStationId) {
        return sections.stream()
                .filter(section -> section.equalsUpStationId(topStationId))
                .findFirst()
                .orElseThrow(() -> new DataNotExistException("다음 역을 찾을 수 없습니다."));
    }

    public boolean isEmpty() {
        return sections.isEmpty();
    }

    public Optional<Section> getUpdatedSectionForSaveIfRequired(Section newSection) {
        validateSectionInLine(newSection);
        if (isRequireUpdateForSave(newSection)) {
            return Optional.of(getUpdatedSectionForSave(newSection));
        }
        return Optional.empty();
    }

    private void validateSectionInLine(Section newSection) {
        List<Long> stationIds = findStationIds();
        boolean existUpStation = stationIds.contains(newSection.getUpStationId());
        boolean existDownStation = stationIds.contains(newSection.getDownStationId());

        validateStationsIncludeInLine(existUpStation, existDownStation);
    }

    private void validateStationsIncludeInLine(boolean existUpStation, boolean existDownStation) {
        if (existUpStation == existDownStation) {
            throw new SubwayException("상행역과 하행역 중 하나만 노선에 포함되어있어야 합니다.");
        }
    }

    private boolean isRequireUpdateForSave(Section newSection) {
        return !(isTopStation(newSection.getDownStationId()) || isBottomStation(newSection.getUpStationId()));
    }

    private Boolean isTopStation(Long stationId) {
        return sections.get(0).equalsUpStationId(stationId);
    }

    private Boolean isBottomStation(Long stationId) {
        return sections.get(sections.size() - 1).equalsDownStationId(stationId);
    }

    private Section getUpdatedSectionForSave(Section newSection) {
        validateSectionDistance(newSection);
        List<Long> stationIds = findStationIds();
        Section existSection = getExistSection(newSection);

        if (stationIds.contains(newSection.getDownStationId())) {
            return existSection.getUpdatedSectionForSameDownStation(newSection);
        }
        return existSection.getUpdatedSectionForSameUpStation(newSection);
    }

    private void validateSectionDistance(Section newSection) {
        if (getExistSection(newSection).isShorterDistance(newSection)) {
            throw new SubwayException("구간의 길이는 기존 역 사이의 길이보다 작아야합니다.");
        }
    }

    private Section getExistSection(Section newSection) {
        return sections.stream()
                .filter(section -> section.equalsUpOrDownStationId(newSection))
                .findFirst()
                .orElseThrow(() -> new DataNotExistException("존재하지 않는 구간입니다."));
    }

    public Optional<Section> getUpdatedSectionForDeleteIfRequired(Long stationId) {
        validateDelete(stationId);
        if(isRequireUpdateForDelete(stationId)) {
            return Optional.of(getUpdatedSectionForDelete(stationId));
        }
        return Optional.empty();
    }

    private void validateDelete(Long stationId) {
        validateNotExistStation(stationId);
        validateLastSection();
    }

    private void validateNotExistStation(Long stationId) {
        if (!findStationIds().contains(stationId)) {
            throw new DataNotExistException("해당 노선에 등록되지 않은 역입니다.");
        }
    }

    private void validateLastSection() {
        if (sections.size() == 1) {
            throw new SubwayException("구간이 하나인 노선에서 마지막 구간을 삭제할 수 없습니다.");
        }
    }

    private boolean isRequireUpdateForDelete(Long stationId) {
        return !(isTopStation(stationId) || isBottomStation(stationId));
    }

    private Section getUpdatedSectionForDelete(Long stationId) {
        Section upSection = getSectionByFilter(section -> section.equalsDownStationId(stationId));
        Section downSection = getSectionByFilter(section -> section.equalsUpStationId(stationId));

        return upSection.getUpdatedSectionForDelete(downSection);
    }

    public Long getDeletedSectionId(Long stationId) {
        if (isBottomStation(stationId)) {
            return getSectionByFilter(section -> section.equalsDownStationId(stationId)).getId();
        }
        return getSectionByFilter(section -> section.equalsUpStationId(stationId)).getId();
    }

    private Section getSectionByFilter(Predicate<Section> sectionPredicate) {
        return sections.stream()
                .filter(sectionPredicate)
                .findFirst()
                .orElseThrow(() -> new DataNotExistException("역이 포함된 구간을 찾을 수 없습니다."));
    }

    public List<Long> findStationIds() {
        List<Long> stationIds = sections.stream()
                .map(Section::getDownStationId)
                .collect(Collectors.toList());
        stationIds.add(0, sections.get(0).getUpStationId());
        return stationIds;
    }
}
