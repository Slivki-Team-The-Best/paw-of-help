package ru.urfu.slivky.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.util.Map;

@Entity
@Table(name = "volunteer_preferences")
@Getter
@Setter
@NoArgsConstructor
public class VolunteerPreference {

    @Id
    private Long userId;

    @OneToOne(fetch = FetchType.LAZY)
    @MapsId
    @JoinColumn(name = "user_id")
    private User user;

    @Column(name = "works_with_cats")
    private Boolean worksWithCats = true;

    @Column(name = "works_with_dogs")
    private Boolean worksWithDogs = true;

    @Column(name = "works_with_shelters")
    private Boolean worksWithShelters = true;

    @Column(name = "works_with_private")
    private Boolean worksWithPrivate = true;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "availability_schedule", columnDefinition = "jsonb")
    private Map<String, String> availabilitySchedule;
}
