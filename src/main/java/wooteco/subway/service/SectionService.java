package wooteco.subway.service;

import java.util.List;
import org.springframework.stereotype.Service;
import wooteco.subway.dao.section.SectionDao;
import wooteco.subway.domain.Section;
import wooteco.subway.domain.Sections;
import wooteco.subway.domain.Stations;

@Service
public class SectionService {

    private final SectionDao sectionDao;

    private final StationService stationService;

    public SectionService(SectionDao sectionDao, StationService stationService) {
        this.sectionDao = sectionDao;
        this.stationService = stationService;
    }

    public Long save(Section section) {
        Sections sections = new Sections(findAllByLineId(section.getLineId()));
        if (!sections.isEmpty()) {
            sections.validateSectionInLine(section);
            updateSectionForSave(section, sections);
        }
        return sectionDao.save(section);
    }

    private void updateSectionForSave(Section section, Sections sections) {
        if (sections.isRequireUpdateForSave(section)) {
            sections.validateSectionDistance(section);
            sectionDao.update(sections.getUpdatedSectionForSave(section));
        }
    }

    public void delete(Long lineId, Long stationId) {
        Sections sections = new Sections(findAllByLineId(lineId));
        sections.validateDelete(stationId);
        if (sections.isRequireUpdateForDelete(stationId)) {
            sectionDao.update(sections.getUpdatedSectionForDelete(stationId));
        }
        sectionDao.delete(sections.getDeletedSectionId(stationId));
    }

    public Stations findStationsByLineId(Long lineId) {
        Sections sections = new Sections(findAllByLineId(lineId));
        Stations stations = stationService.findAll();
        List<Long> stationIds = sections.findStationIds();
        return stations.filter(stationIds);
    }

    public List<Section> findAllByLineId(Long lineId) {
        return sectionDao.findAllByLineId(lineId);
    }

    public List<Section> findAll() {
        return sectionDao.findAll();
    }
}
