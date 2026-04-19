package ru.urfu.slivky.service;

import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.urfu.slivky.exception.NotFoundException;
import ru.urfu.slivky.model.Shelter;
import ru.urfu.slivky.model.User;
import ru.urfu.slivky.model.UserRole;
import ru.urfu.slivky.repository.ShelterRepository;
import ru.urfu.slivky.security.CurrentUser;
import ru.urfu.slivky.web.dto.ShelterCreateRequest;
import ru.urfu.slivky.web.dto.ShelterResponse;
import ru.urfu.slivky.web.dto.ShelterUpdateRequest;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ShelterService {

    private final ShelterRepository shelterRepository;

    @Transactional(readOnly = true)
    public ShelterResponse getById(Long id) {
        Shelter s = shelterRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Shelter not found"));
        return toResponse(s);
    }

    @Transactional(readOnly = true)
    public List<ShelterResponse> listMine() {
        User actor = CurrentUser.get();
        return shelterRepository.findByCreatedByIdOrderByCreatedAtDesc(actor.getId()).stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional
    public ShelterResponse create(ShelterCreateRequest req) {
        User actor = CurrentUser.get();
        if (!(actor.getRole() == UserRole.SHELTER_STAFF || actor.getRole() == UserRole.ADMIN)) {
            throw new AccessDeniedException("Only shelter staff can create shelter profiles");
        }
        Shelter s = new Shelter();
        s.setName(req.name().trim());
        s.setDescription(req.description());
        s.setAddress(req.address());
        s.setPhone(req.phone());
        s.setEmail(req.email());
        s.setWebsite(req.website());
        s.setCreatedBy(actor);
        shelterRepository.save(s);
        return toResponse(s);
    }

    @Transactional
    public ShelterResponse update(Long id, ShelterUpdateRequest req) {
        Shelter s = shelterRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Shelter not found"));
        User actor = CurrentUser.get();
        assertCanManage(s, actor);

        if (req.name() != null && !req.name().isBlank()) {
            s.setName(req.name().trim());
        }
        if (req.description() != null) {
            s.setDescription(req.description());
        }
        if (req.address() != null) {
            s.setAddress(req.address());
        }
        if (req.phone() != null) {
            s.setPhone(req.phone().isBlank() ? null : req.phone().trim());
        }
        if (req.email() != null) {
            s.setEmail(req.email().isBlank() ? null : req.email().trim());
        }
        if (req.website() != null) {
            s.setWebsite(req.website().isBlank() ? null : req.website().trim());
        }
        return toResponse(s);
    }

    private void assertCanManage(Shelter s, User actor) {
        if (actor.getRole() == UserRole.ADMIN) {
            return;
        }
        if (s.getCreatedBy() != null && s.getCreatedBy().getId().equals(actor.getId())) {
            return;
        }
        throw new AccessDeniedException("You cannot edit this shelter");
    }

    private ShelterResponse toResponse(Shelter s) {
        return new ShelterResponse(
                s.getId(),
                s.getName(),
                s.getDescription(),
                s.getAddress(),
                s.getPhone(),
                s.getEmail(),
                s.getWebsite(),
                s.getVerified(),
                s.getCreatedBy() != null ? s.getCreatedBy().getId() : null,
                s.getCreatedAt()
        );
    }
}
