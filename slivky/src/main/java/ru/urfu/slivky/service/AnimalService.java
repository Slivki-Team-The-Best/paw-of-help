package ru.urfu.slivky.service;

import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.urfu.slivky.exception.BadRequestException;
import ru.urfu.slivky.exception.NotFoundException;
import ru.urfu.slivky.model.Animal;
import ru.urfu.slivky.model.User;
import ru.urfu.slivky.model.UserRole;
import ru.urfu.slivky.repository.AnimalRepository;
import ru.urfu.slivky.repository.ShelterRepository;
import ru.urfu.slivky.security.CurrentUser;
import ru.urfu.slivky.web.dto.AnimalCreateRequest;
import ru.urfu.slivky.web.dto.AnimalResponse;

import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class AnimalService {

    private static final Set<String> ALLOWED_GENDER = Set.of("MALE", "FEMALE", "UNKNOWN");
    private static final Set<String> ALLOWED_STATUS = Set.of(
            "SEEKING_HELP", "IN_TREATMENT", "ADOPTED", "FOSTERED", "DECEASED");

    private final AnimalRepository animalRepository;
    private final ShelterRepository shelterRepository;

    @Transactional(readOnly = true)
    public List<AnimalResponse> list(Long shelterId) {
        var source = shelterId == null
                ? animalRepository.findAllByOrderByCreatedAtDesc()
                : animalRepository.findByShelter_IdOrderByCreatedAtDesc(shelterId);
        return source.stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public AnimalResponse get(Long id) {
        Animal animal = animalRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Animal not found"));
        return toResponse(animal);
    }

    @Transactional
    public AnimalResponse create(AnimalCreateRequest req) {
        User actor = CurrentUser.get();
        if (!(actor.getRole() == UserRole.PET_OWNER
                || actor.getRole() == UserRole.SHELTER_STAFF
                || actor.getRole() == UserRole.ADMIN)) {
            throw new AccessDeniedException("Only pet owners or shelter staff can register animals");
        }

        Animal animal = new Animal();
        animal.setName(req.name());
        animal.setType(req.type());
        animal.setBreed(req.breed());
        animal.setAge(req.age());
        if (req.gender() != null && !req.gender().isBlank()) {
            String g = req.gender().trim();
            if (!ALLOWED_GENDER.contains(g)) {
                throw new BadRequestException("gender must be one of: MALE, FEMALE, UNKNOWN");
            }
            animal.setGender(g);
        }
        animal.setDescription(req.description());
        animal.setHealthStatus(req.healthStatus());
        animal.setSpecialNeeds(req.specialNeeds());
        if (req.photoUrls() != null && !req.photoUrls().isEmpty()) {
            animal.setPhotoUrls(req.photoUrls().toArray(String[]::new));
        }
        if (req.status() != null && !req.status().isBlank()) {
            String st = req.status().trim();
            if (!ALLOWED_STATUS.contains(st)) {
                throw new BadRequestException("Invalid status value");
            }
            animal.setStatus(st);
        }
        animal.setCreatedBy(actor);

        if (req.shelterId() != null) {
            animal.setShelter(shelterRepository.findById(req.shelterId())
                    .orElseThrow(() -> new BadRequestException("Shelter not found")));
        }

        animalRepository.save(animal);
        return toResponse(animal);
    }

    private AnimalResponse toResponse(Animal a) {
        return new AnimalResponse(
                a.getId(),
                a.getName(),
                a.getType(),
                a.getBreed(),
                a.getAge(),
                a.getGender(),
                a.getDescription(),
                a.getHealthStatus(),
                a.getSpecialNeeds(),
                a.getPhotoUrls(),
                a.getStatus(),
                a.getShelter() != null ? a.getShelter().getId() : null,
                a.getCreatedBy() != null ? a.getCreatedBy().getId() : null,
                a.getCreatedAt()
        );
    }
}
